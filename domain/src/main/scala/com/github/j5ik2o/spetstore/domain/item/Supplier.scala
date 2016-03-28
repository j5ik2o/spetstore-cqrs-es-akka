package com.github.j5ik2o.spetstore.domain.item

import com.github.j5ik2o.spetstore.domain.basic.{Contact, PostalAddress, StatusType}
import com.github.j5ik2o.spetstore.infrastructure.domainsupport._

trait SupplierEvent extends Event

trait SupplierCreateEvent extends SupplierEvent with CreateEvent
trait SupplierUpdateEvent extends SupplierEvent with UpdateEvent

object SupplierEvent {

  case class NameUpdated(id: EventId, entityId: SupplierId, name: String)
    extends SupplierUpdateEvent

}
/**
  * 仕入れ先を表すエンティティ。
  *
  * @param id            [[SupplierId]]
  * @param name          名前
  * @param postalAddress 住所
  * @param contact       連絡先
  */
case class Supplier
(id: SupplierId,
 status: StatusType.Value,
 name: String,
 postalAddress: PostalAddress,
 contact: Contact,
 version: Option[Long])
  extends BaseEntity[SupplierId] {

  override type This = Supplier

  override type Event = SupplierUpdateEvent

  override def withVersion(version: Long): Entity[SupplierId] =
    copy(version = Some(version))


  override def updateState: StateMachine = {
    case SupplierEvent.NameUpdated(_, entityId, value) =>
      require(id == entityId)
      copy(name = value)
  }

}


