package com.github.j5ik2o.spetstore.domain.purchase

import com.github.j5ik2o.spetstore.domain.basic.StatusType
import com.github.j5ik2o.spetstore.domain.customer.CustomerId
import com.github.j5ik2o.spetstore.domain.item.ItemId
import com.github.j5ik2o.spetstore.infrastructure.domainsupport._

trait CartEvent extends Event
trait CartCreateEvent extends CartEvent with CreateEvent
trait CartUpdateEvent extends CartEvent with UpdateEvent

object CartEvent {

  case class CartItemsAdded(id: EventId, entityId: CartId, cartItems: Seq[CartItem])
    extends CartUpdateEvent

  case class CartItemsRemoved(id: EventId, entityId: CartId, cartItemIds: Seq[CartItemId])
    extends CartUpdateEvent

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
) extends BaseEntity[CartId] {

  override type This = Cart

  override type Event = CartUpdateEvent

  override def updateState: StateMachine = {
    case CartEvent.CartItemsAdded(_, entityId, values) =>
      require(id == entityId)
      copy(cartItems = cartItems ++ values)
    case CartEvent.CartItemsRemoved(_, entityId, values) =>
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
