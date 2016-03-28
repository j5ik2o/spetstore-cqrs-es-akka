package com.github.j5ik2o.spetstore.domain.purchase

import com.github.j5ik2o.spetstore.domain.basic.{Contact, PostalAddress, StatusType}
import com.github.j5ik2o.spetstore.domain.customer.CustomerId
import com.github.j5ik2o.spetstore.domain.model.purchase.OrderStatus
import com.github.j5ik2o.spetstore.infrastructure.domainsupport._
import org.joda.time.DateTime

import scala.collection.mutable.ListBuffer

trait OrderEvent extends Event

trait OrderCreateEvent extends OrderEvent with CreateEvent
trait OrderUpdateEvent extends OrderEvent with UpdateEvent

object OrderEvent {

  case class CustomerNameUpdated(id: EventId, entityId: OrderId, name: String)
    extends OrderUpdateEvent

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
case class Order
(id: OrderId,
 status: StatusType.Value,
 orderStatus: OrderStatus.Value,
 orderDate: DateTime,
 customerId: CustomerId,
 customerName: String,
 shippingAddress: PostalAddress,
 shippingContact: Contact,
 orderItems: List[OrderItem],
 version: Option[Long])
  extends BaseEntity[OrderId] {

  override type This = Order

  override type Event = OrderUpdateEvent

  override def updateState: StateMachine = {
    case OrderEvent.CustomerNameUpdated(_, entityId, value) =>
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
