package com.github.j5ik2o.spetstore.domain.item

import com.github.j5ik2o.spetstore.domain.basic.{ Contact, PostalAddress, StatusType }
import com.github.j5ik2o.spetstore.domain.item.SupplierAggregateProtocol.Create.{ SupplierCreateEvent, SupplierCreated }
import com.github.j5ik2o.spetstore.domain.item.SupplierAggregateProtocol.Update.{ NameUpdated, SupplierUpdateEvent }
import com.github.j5ik2o.spetstore.infrastructure.domainsupport.EntityProtocol.{ CommandRequestId, EventId }
import com.github.j5ik2o.spetstore.infrastructure.domainsupport._

object SupplierAggregateProtocol extends EntityProtocol {
  type Id = SupplierId
  type CommandRequest = SupplierCommandRequest
  type CommandResponse = SupplierCommandResponse
  type Event = SupplierEvent
  type QueryRequest = SupplierQueryRequest
  type QueryResponse = SupplierQueryResponse

  sealed trait SupplierCommandRequest extends EntityProtocol.CommandRequest[Id]

  sealed trait SupplierCommandResponse extends EntityProtocol.CommandResponse[Id]

  sealed trait SupplierEvent extends EntityProtocol.Event[Id]

  sealed trait SupplierQueryRequest extends EntityProtocol.QueryRequest[Id]

  sealed trait SupplierQueryResponse extends EntityProtocol.QueryResponse[Id]

  object Create {

    trait SupplierCreateCommandRequest extends SupplierCommandRequest with EntityProtocol.CreateCommandRequest[Id] {
      override def toEvent: SupplierCreateEvent
    }

    case class CreateSupplier(
        id:            CommandRequestId,
        entityId:      Id,
        name:          String,
        postalAddress: PostalAddress,
        contact:       Contact
    ) extends SupplierCreateCommandRequest {
      override def toEvent: SupplierCreateEvent =
        SupplierCreated(EventId(), entityId, name, postalAddress, contact)
    }

    case class CreateSucceeded(id: EntityProtocol.CommandResponseId, commandRequestId: EntityProtocol.CommandRequestId, entityId: Id)
      extends SupplierCommandResponse with EntityProtocol.CommandSucceeded[Id, Supplier]

    case class CreateFailed(id: EntityProtocol.CommandResponseId, commandRequestId: EntityProtocol.CommandRequestId, entityId: Id, throwable: Throwable)
      extends SupplierCommandResponse with EntityProtocol.CommandFailed[Id]

    trait SupplierCreateEvent extends SupplierEvent with EntityProtocol.CreateEvent[Id]

    case class SupplierCreated(
      id:            EventId,
      entityId:      Id,
      name:          String,
      postalAddress: PostalAddress,
      contact:       Contact
    ) extends SupplierCreateEvent

  }

  object Update {

    trait SupplierUpdateCommandRequest extends SupplierCommandRequest with EntityProtocol.UpdateCommandRequest[Id] {
      override def toEvent: SupplierUpdateEvent
    }

    case class UpdateName(
      id:       CommandRequestId,
      entityId: Id,
      name:     String
    )
        extends SupplierUpdateCommandRequest {
      override def toEvent: SupplierUpdateEvent =
        NameUpdated(EntityProtocol.EventId(), entityId, name)
    }

    case class UpdateSucceeded(id: EntityProtocol.CommandResponseId, commandRequestId: EntityProtocol.CommandRequestId, entityId: Id)
      extends SupplierCommandResponse with EntityProtocol.CommandSucceeded[Id, Supplier]

    case class UpdateFailed(id: EntityProtocol.CommandResponseId, commandRequestId: EntityProtocol.CommandRequestId, entityId: Id, throwable: Throwable)
      extends SupplierCommandResponse with EntityProtocol.CommandFailed[Id]

    trait SupplierUpdateEvent extends SupplierEvent with EntityProtocol.UpdateEvent[Id]

    case class NameUpdated(id: EventId, entityId: Id, name: String)
      extends SupplierUpdateEvent

  }

  object Query {

    case class GetStateRequest(id: EntityProtocol.QueryRequestId, entityId: Id) extends EntityProtocol.GetStateRequest[Id]

    case class GetStateResponse(id: EntityProtocol.QueryResponseId, queryRequestId: EntityProtocol.QueryRequestId, entityId: Id, entity: Option[Supplier])
      extends EntityProtocol.GetStateResponse[Id, Supplier]

  }

}

object Supplier extends EntityFactory[SupplierId, Supplier, SupplierCreateEvent, SupplierUpdateEvent] {

  override def createFromEvent: PartialFunction[SupplierCreateEvent, Supplier] = {
    case SupplierCreated(_, id, name, postalAddress, contact) =>
      Supplier(id, StatusType.Enabled, name, postalAddress, contact, Some(1L))
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
  id:            SupplierId,
  status:        StatusType.Value,
  name:          String,
  postalAddress: PostalAddress,
  contact:       Contact,
  version:       Option[Long]
)
    extends BaseEntity[SupplierId, SupplierUpdateEvent] {

  override type This = Supplier

  override def withVersion(version: Long): Entity[SupplierId] =
    copy(version = Some(version))

  override def updateState: StateMachine = {
    case NameUpdated(_, entityId, value) =>
      require(id == entityId)
      copy(name = value)
  }

}

