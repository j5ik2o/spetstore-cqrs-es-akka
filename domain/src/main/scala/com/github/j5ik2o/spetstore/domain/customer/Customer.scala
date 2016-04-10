package com.github.j5ik2o.spetstore.domain.customer

import java.util.UUID

import com.github.j5ik2o.spetstore.domain.basic.StatusType
import com.github.j5ik2o.spetstore.domain.customer.CustomerAggregateProtocol.Create.{ CustomerCreateEvent, CustomerCreated }
import com.github.j5ik2o.spetstore.domain.customer.CustomerAggregateProtocol.Update.{ CustomerUpdateEvent, NameUpdated }
import com.github.j5ik2o.spetstore.infrastructure.domainsupport
import com.github.j5ik2o.spetstore.infrastructure.domainsupport.{ BaseEntity, Entity, EntityFactory, EntityProtocol }

object CustomerAggregateProtocol extends domainsupport.EntityProtocol {
  type Id = CustomerId
  type CommandRequest = CustomerCommandRequest
  type CommandResponse = CustomerCommandResponse
  type Event = CustomerEvent
  type QueryRequest = CustomerQueryRequest
  type QueryResponse = CustomerQueryResponse

  sealed trait CustomerCommandRequest extends EntityProtocol.CommandRequest[CustomerId]

  sealed trait CustomerCommandResponse extends EntityProtocol.CommandResponse[CustomerId]

  sealed trait CustomerEvent extends EntityProtocol.Event[CustomerId]

  sealed trait CustomerQueryRequest extends EntityProtocol.QueryRequest[CustomerId]

  sealed trait CustomerQueryResponse extends EntityProtocol.QueryResponse[CustomerId]

  object Create {

    trait CustomerCreateCommandRequest extends CustomerCommandRequest with EntityProtocol.CreateCommandRequest[CustomerId] {
      override def toEvent: CustomerCreateEvent
    }

    case class CreateCustomer(
      id:       EntityProtocol.CommandRequestId,
      entityId: CustomerId,
      status:   StatusType.Value,
      profile:  CustomerProfile,
      config:   CustomerConfig,
      version:  Option[Long]
    )
        extends CustomerCreateCommandRequest {
      override def toEvent: CustomerCreated = CustomerCreated(
        EntityProtocol.EventId(UUID.randomUUID()),
        entityId,
        status,
        profile,
        config,
        version
      )
    }

    case class CreateSucceeded(id: EntityProtocol.CommandResponseId, commandRequestId: EntityProtocol.CommandRequestId, entityId: CustomerId)
      extends CustomerCommandResponse with EntityProtocol.CommandSucceeded[CustomerId, Customer]

    case class CreateFailed(id: EntityProtocol.CommandResponseId, commandRequestId: EntityProtocol.CommandRequestId, entityId: CustomerId, throwable: Throwable)
      extends EntityProtocol.CommandFailed[CustomerId]

    trait CustomerCreateEvent extends CustomerEvent with EntityProtocol.CreateEvent[CustomerId]

    case class CustomerCreated(
      id:       EntityProtocol.EventId,
      entityId: CustomerId,
      status:   StatusType.Value,
      profile:  CustomerProfile,
      config:   CustomerConfig,
      version:  Option[Long]
    )
        extends CustomerCreateEvent

  }

  object Update {

    trait CustomerUpdateCommandRequest extends CustomerCommandRequest with EntityProtocol.UpdateCommandRequest[Id] {
      override def toEvent: CustomerUpdateEvent
    }

    case class UpdateName(id: EntityProtocol.CommandRequestId, entityId: Id, name: String) extends CustomerUpdateCommandRequest {
      override def toEvent: CustomerUpdateEvent = NameUpdated(EntityProtocol.EventId(UUID.randomUUID()), entityId, name)
    }

    case class UpdateSucceeded(id: EntityProtocol.CommandResponseId, commandRequestId: EntityProtocol.CommandRequestId, entityId: Id)
      extends CustomerCommandResponse with EntityProtocol.CommandSucceeded[Id, Customer]

    case class UpdateFailed(id: EntityProtocol.CommandResponseId, commandRequestId: EntityProtocol.CommandRequestId, entityId: Id, throwable: Throwable)
      extends CustomerCommandResponse with EntityProtocol.CommandFailed[Id]

    trait CustomerUpdateEvent extends CustomerEvent with EntityProtocol.UpdateEvent[Id]

    case class NameUpdated(id: EntityProtocol.EventId, entityId: Id, name: String) extends CustomerUpdateEvent

  }

  object Delete {

  }

  object Query {

    case class GetStateRequest(id: EntityProtocol.QueryRequestId, entityId: Id) extends EntityProtocol.GetStateRequest[Id]

    case class GetStateResponse(id: EntityProtocol.QueryResponseId, queryRequestId: EntityProtocol.QueryRequestId, entityId: Id, entity: Option[Customer])
      extends EntityProtocol.GetStateResponse[Id, Customer]

  }

}

object Customer extends EntityFactory[CustomerId, Customer, CustomerCreateEvent, CustomerUpdateEvent] {

  override def createFromEvent: PartialFunction[CustomerCreateEvent, Customer] = {
    case CustomerCreated(_, customerId, status, profile, config, version) =>
      new Customer(customerId, status, profile, config, version)
  }
}

/**
 * ペットストアの顧客を表すエンティティ。
 *
 * @param id      識別子
 * @param status  [[StatusType]]
 * @param profile [[CustomerProfile]]
 * @param config  [[CustomerConfig]]
 */
case class Customer(
  id:      CustomerId,
  status:  StatusType.Value,
  profile: CustomerProfile,
  config:  CustomerConfig,
  version: Option[Long]
)
    extends BaseEntity[CustomerId, CustomerUpdateEvent] {

  override type This = Customer

  override def withVersion(version: Long): Entity[CustomerId] = copy(version = Some(version))

  override def updateState: StateMachine = {
    case NameUpdated(_, entityId, value) =>
      require(entityId == id)
      copy(profile = profile.copy(name = value))
  }

}

