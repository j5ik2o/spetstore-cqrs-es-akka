package com.github.j5ik2o.spetstore.domain.purchase

import com.github.j5ik2o.spetstore.domain.basic.StatusType
import com.github.j5ik2o.spetstore.domain.customer.CustomerId
import com.github.j5ik2o.spetstore.domain.item.ItemId
import com.github.j5ik2o.spetstore.domain.purchase.CartAggregateProtocol.Create.{ CartCreateEvent, CartCreated }
import com.github.j5ik2o.spetstore.domain.purchase.CartAggregateProtocol.Update.{ CartItemsAdded, CartItemsRemoved, CartUpdateEvent }
import com.github.j5ik2o.spetstore.infrastructure.domainsupport
import com.github.j5ik2o.spetstore.infrastructure.domainsupport.EntityProtocol.{ CommandRequestId, EventId }
import com.github.j5ik2o.spetstore.infrastructure.domainsupport.{ BaseEntity, Entity, EntityFactory, EntityProtocol }

object CartAggregateProtocol extends domainsupport.EntityProtocol {
  type Id = CartId
  type CommandRequest = CartCommandRequest
  type CommandResponse = CartCommandResponse
  type Event = CartEvent
  type QueryRequest = CartQueryRequest
  type QueryResponse = CartQueryResponse

  sealed trait CartCommandRequest extends EntityProtocol.CommandRequest[Id]

  sealed trait CartCommandResponse extends EntityProtocol.CommandResponse[Id]

  sealed trait CartEvent extends EntityProtocol.Event[Id]

  sealed trait CartQueryRequest extends EntityProtocol.QueryRequest[Id]

  sealed trait CartQueryResponse extends EntityProtocol.QueryResponse[Id]

  object Create {

    trait CartCreateCommandRequest extends CartCommandRequest with EntityProtocol.CreateCommandRequest[Id] {
      override def toEvent: CartCreateEvent
    }

    case class CreateCart(
        id:         CommandRequestId,
        entityId:   CartId,
        customerId: CustomerId,
        cartItems:  List[CartItem]
    ) extends CartCreateCommandRequest {
      override def toEvent: CartCreateEvent =
        CartCreated(EventId(), entityId, customerId, cartItems)
    }

    case class CreateSucceeded(id: EntityProtocol.CommandResponseId, commandRequestId: EntityProtocol.CommandRequestId, entityId: Id)
      extends CartCommandResponse with EntityProtocol.CommandSucceeded[Id, Cart]

    case class CreateFailed(id: EntityProtocol.CommandResponseId, commandRequestId: EntityProtocol.CommandRequestId, entityId: Id, throwable: Throwable)
      extends CartCommandResponse with EntityProtocol.CommandFailed[Id]

    trait CartCreateEvent extends CartEvent with EntityProtocol.CreateEvent[Id]

    case class CartCreated(
      id:         EventId,
      entityId:   Id,
      customerId: CustomerId,
      cartItems:  List[CartItem]
    )
        extends CartCreateEvent

  }

  object Update {

    trait CartUpdateCommandRequest extends CartCommandRequest with EntityProtocol.UpdateCommandRequest[Id] {
      override def toEvent: CartUpdateEvent
    }

    case class UpdateSucceeded(id: EntityProtocol.CommandResponseId, commandRequestId: EntityProtocol.CommandRequestId, entityId: Id)
      extends CartCommandResponse with EntityProtocol.CommandSucceeded[Id, Cart]

    case class UpdateFailed(id: EntityProtocol.CommandResponseId, commandRequestId: EntityProtocol.CommandRequestId, entityId: Id, throwable: Throwable)
      extends CartCommandResponse with EntityProtocol.CommandFailed[Id]

    trait CartUpdateEvent extends CartEvent with EntityProtocol.UpdateEvent[Id]

    case class CartItemsAdded(id: EntityProtocol.EventId, entityId: CartId, cartItems: Seq[CartItem])
      extends CartUpdateEvent

    case class CartItemsRemoved(id: EntityProtocol.EventId, entityId: CartId, cartItemIds: Seq[CartItemId])
      extends CartUpdateEvent

  }

  object Query {

    case class GetStateRequest(id: EntityProtocol.QueryRequestId, entityId: Id) extends EntityProtocol.GetStateRequest[Id]

    case class GetStateResponse(id: EntityProtocol.QueryResponseId, queryRequestId: EntityProtocol.QueryRequestId, entityId: Id, entity: Option[Cart])
      extends EntityProtocol.GetStateResponse[Id, Cart]

  }

}

object Cart extends EntityFactory[CartId, Cart, CartCreateEvent, CartUpdateEvent] {
  override def createFromEvent: PartialFunction[CartCreateEvent, Cart] = {
    case CartCreated(_, id, customerId, cartItems) =>
      Cart(id, StatusType.Enabled, customerId, cartItems, Some(1L))
  }
}

/**
 * ショッピングカートを表すエンティティ。
 *
 * @param cartItems [[CartItem]]のリスト
 */
case class Cart(
    id:         CartId,
    status:     StatusType.Value,
    customerId: CustomerId,
    cartItems:  List[CartItem],
    version:    Option[Long]
) extends BaseEntity[CartId, CartUpdateEvent] {

  override type This = Cart

  override def updateState: StateMachine = {
    case CartItemsAdded(_, entityId, values) =>
      require(id == entityId)
      copy(cartItems = cartItems ++ values)
    case CartItemsRemoved(_, entityId, values) =>
      require(id == entityId)
      copy(cartItems = cartItems.filterNot { e => values.contains(e) })
  }

  /**
   * [[CartItem]]の個数。
   */
  val sizeOfCartItems = cartItems.size

  /**
   * [[CartItem]]の総数。
   */
  val quantityOfCartItems = cartItems.foldLeft(0)(_ + _.quantity)

  /**
   * [[ItemId]]が含まれるかを検証する。
   *
   * @param itemId [[ItemId]]
   * @return 含まれる場合はtrue
   */
  def containsItemId(itemId: ItemId): Boolean =
    cartItems.exists {
      _.itemId == itemId
    }

  /**
   * このカートに[[CartItem]]を追加する。
   *
   * @param cartItem [[CartItem]]
   * @return 新しい[[Cart]]
   */
  def addCartItem(cartItem: CartItem): Cart = {
    require(cartItem.quantity > 0)
    cartItems.find(_.itemId == cartItem.itemId).map {
      currentItem =>
        val newCartItem = currentItem.addQuantity(cartItem.quantity).ensuring(_.quantity > 0)
        copy(cartItems = newCartItem :: cartItems.filterNot(_.itemId == cartItem.itemId))
    }.getOrElse {
      copy(cartItems = cartItem :: cartItems)
    }
  }

  /**
   * このカートに[[CartItem]]を追加する。
   *
   * @param itemId    [[ItemId]]
   * @param quantity  個数
   * @param isInStock ストックする場合true
   * @return 新しい[[Cart]]
   */
  def addCartItem(cartItemId: CartItemId, itemId: ItemId, quantity: Int, isInStock: Boolean = false): Cart =
    addCartItem(CartItem(cartItemId, StatusType.Enabled, cartItems.size + 1, itemId, quantity, isInStock, None))

  def removeCartItemId(cartItemId: CartItemId): Cart =
    copy(
      cartItems = cartItems.filterNot(_.id == cartItemId)
    )

  /**
   * [[ItemId]]を使って
   * [[CartItem]]を
   * 削除する。
   *
   * @param itemId [[ItemId]]
   * @return 新しい[[Cart]]
   */
  def removeCartItemByItemId(itemId: ItemId): Cart =
    cartItems.find(_.itemId == itemId).map {
      e =>
        copy(cartItems = cartItems.filterNot(_.itemId == itemId))
    }.getOrElse(this)

  /**
   * 特定の[[CartItem]]の数量を
   * インクリメントする。
   *
   * @param itemId [[ItemId]]
   * @return 新しい[[Cart]]
   */
  def incrementQuantityByItemId(itemId: ItemId): Cart =
    cartItems.find(_.itemId == itemId).map {
      cartItem =>
        val newCartItem = cartItem.incrementQuantity.ensuring(_.quantity > 0)
        copy(cartItems = newCartItem :: cartItems.filterNot(_.itemId == itemId))
    }.getOrElse(this)

  /**
   * 特定の[[CartItem]]の数量を更新する。
   *
   * @param itemId   [[ItemId]]
   * @param quantity 数量
   * @return 新しい[[Cart]]
   */
  def updateQuantityByItemId(itemId: ItemId, quantity: Int): Cart = {
    require(quantity > 0)
    cartItems.find(_.itemId == itemId).map {
      cartItem =>
        val newCartItem = cartItem.withQuantity(quantity).ensuring(_.quantity > 0)
        copy(cartItems = newCartItem :: cartItems.filterNot(_.itemId == itemId))
    }.getOrElse(this)
  }

  override def withVersion(version: Long): Entity[CartId] = copy(version = Some(version))

}
