package com.github.j5ik2o.spetstore.domain.item

import com.github.j5ik2o.spetstore.domain.basic.{Contact, PostalAddress, StatusType}
import com.github.j5ik2o.spetstore.domain.item.SupplierAggregateProtocol.Update.{NameUpdated, SupplierUpdateEvent}
import com.github.j5ik2o.spetstore.infrastructure.domainsupport.EntityProtocol.EventId
import com.github.j5ik2o.spetstore.infrastructure.domainsupport._

object SupplierAggregateProtocol extends EntityProtocol {
  override type Id = SupplierId
  override type CommandRequest = SupplierCommandRequest
  override type CommandResponse = SupplierCommandResponse
  override type Event = SupplierEvent
  override type QueryRequest = SupplierQueryRequest
  override type QueryResponse = SupplierQueryResponse

  sealed trait SupplierCommandRequest extends EntityProtocol.CommandRequest[Id]

  sealed trait SupplierCommandResponse extends EntityProtocol.CommandResponse[Id]

  sealed trait SupplierEvent extends EntityProtocol.Event[Id]

  sealed trait SupplierQueryRequest extends EntityProtocol.QueryRequest[Id]

  sealed trait SupplierQueryResponse extends EntityProtocol.QueryResponse[Id]

  object Create {

    trait SupplierCreateEvent extends SupplierEvent with EntityProtocol.CreateEvent[Id]

  }

  object Update {

    trait SupplierUpdateEvent extends SupplierEvent with EntityProtocol.UpdateEvent[Id]

    case class NameUpdated(id: EventId, entityId: Id, name: String)
      extends SupplierUpdateEvent

  }


}

/**
  * 仕入れ先を表すエンティティ。
  *
  * @param id            [[SupplierId]]
  * @param name          名前
  * @param postalAddress 住所
  * @param contact       連絡先
  */
case class Supplier(
                     id: SupplierId,
                     status: StatusType.Value,
                     name: String,
                     postalAddress: PostalAddress,
                     contact: Contact,
                     version: Option[Long]
                   )
  extends BaseEntity[SupplierId] {

  override type This = Supplier

  override type Event = SupplierUpdateEvent

  override def withVersion(version: Long): Entity[SupplierId] =
    copy(version = Some(version))

  override def updateState: StateMachine = {
    case NameUpdated(_, entityId, value) =>
      require(id == entityId)
      copy(name = value)
  }

}

