package com.github.j5ik2o.spetstore.domain.purchase

import com.github.j5ik2o.spetstore.domain.basic.{ Contact, PostalAddress, StatusType }
import com.github.j5ik2o.spetstore.domain.customer.CustomerId
import com.github.j5ik2o.spetstore.domain.model.purchase.OrderStatus
import com.github.j5ik2o.spetstore.domain.purchase.OrderAggregateProtocol.OrderEvent.{ CustomerNameUpdated, OrderUpdateEvent }
import com.github.j5ik2o.spetstore.infrastructure.domainsupport.EntityProtocol.EventId
import com.github.j5ik2o.spetstore.infrastructure.domainsupport._
import org.joda.time.DateTime

import scala.collection.mutable.ListBuffer

object OrderAggregateProtocol extends EntityProtocol {

  override type Id = OrderId
  override type CommandRequest = OrderCommandRequest
  override type CommandResponse = OrderCommandResponse
  override type Event = OrderEvent
  override type QueryRequest = OrderQueryRequest
  override type QueryResponse = OrderQueryResponse

  sealed trait OrderCommandRequest extends EntityProtocol.CommandRequest[Id]

  sealed trait OrderCommandResponse extends EntityProtocol.CommandResponse[Id]

  sealed trait OrderEvent extends EntityProtocol.Event[Id]

  sealed trait OrderQueryRequest extends EntityProtocol.QueryRequest[Id]

  sealed trait OrderQueryResponse extends EntityProtocol.QueryResponse[Id]

  object Create {

    trait OrderCreateEvent extends OrderEvent with EntityProtocol.CreateEvent[Id]

  }

  object OrderEvent {

    trait OrderUpdateEvent extends OrderEvent with EntityProtocol.UpdateEvent[Id]

    case class CustomerNameUpdated(id: EventId, entityId: Id, name: String)
      extends OrderUpdateEvent

  }

}

/**
 * 注文を表すエンティティ。
 *
 * @param id              識別子
 * @param orderDate       注文日時
 * @param customerName    購入者名
 * @param shippingAddress 出荷先の住所
 * @param shippingContact 出荷先の連絡先
 * @param orderItems      [[OrderItem]]のリスト
 */
case class Order(
  id:              OrderId,
  status:          StatusType.Value,
  orderStatus:     OrderStatus.Value,
  orderDate:       DateTime,
  customerId:      CustomerId,
  customerName:    String,
  shippingAddress: PostalAddress,
  shippingContact: Contact,
  orderItems:      List[OrderItem],
  version:         Option[Long]
)
    extends BaseEntity[OrderId, OrderUpdateEvent] {

  override type This = Order

  override def updateState: StateMachine = {
    case CustomerNameUpdated(_, entityId, value) =>
      require(id == entityId)
      copy(customerName = value)
  }

  /**
   * [[OrderItem]]の個数
   */
  val sizeOfOrderItems: Int = orderItems.size

  /**
   * [[OrderItem]]の総数。
   */
  lazy val quantityOfOrderItems = orderItems.foldLeft(0)(_ + _.quantity)

  /**
   * この注文に[[OrderItem]]を追加する。
   *
   * @param orderItem [[OrderItem]]
   * @return 新しい[[Order]]
   */
  def addOrderItem(orderItem: OrderItem): Order =
    copy(orderItems = orderItem :: orderItems)

  /**
   * この注文から[[OrderItem]]を削除する。
   *
   * @param orderItem [[OrderItem]]
   * @return 新しい[[Order]]
   */
  def removeOrderItem(orderItem: OrderItem): Order =
    if (orderItems.contains(orderItem)) {
      copy(orderItems = orderItems.filterNot(_ == orderItem))
    } else {
      this
    }

  /**
   * この注文から指定したインデックスの
   * [[OrderItem]]を削除する。
   *
   * @param index インデックス
   * @return 新しい[[Order]]
   */
  def removeOrderItemByIndex(index: Int): Order = {
    require(orderItems.size > index)
    val lb = ListBuffer(orderItems: _*)
    lb.remove(index)
    copy(orderItems = lb.result())
  }

  override def withVersion(version: Long): Entity[OrderId] = copy(version = Some(version))

}
