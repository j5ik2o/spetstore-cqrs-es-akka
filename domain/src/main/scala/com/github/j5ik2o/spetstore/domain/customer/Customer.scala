package com.github.j5ik2o.spetstore.domain.customer

import java.util.UUID

import com.github.j5ik2o.spetstore.domain.basic.{SexType, StatusType}
import com.github.j5ik2o.spetstore.domain.customer.CustomerAggregateProtocol.Create.CustomerCreated
import com.github.j5ik2o.spetstore.domain.customer.CustomerAggregateProtocol.Update.{CustomerUpdateEvent, NameUpdated}
import com.github.j5ik2o.spetstore.infrastructure.domainsupport.{BaseEntity, Entity, EntityFactory, EntityProtocol}
import com.github.j5ik2o.spetstore.infrastructure.domainsupport.EntityProtocol._


object CustomerAggregateProtocol extends EntityProtocol {
  override type Id = CustomerId
  override type CommandRequest = CustomerCommandRequest
  override type CommandResponse = CustomerCommandResponse
  override type Event = CustomerEvent
  override type QueryRequest = CustomerQueryRequest
  override type QueryResponse = CustomerQueryResponse

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
                               id: CommandRequestId,
                               entityId: CustomerId,
                               status: StatusType.Value,
                               name: String,
                               sexType: SexType.Value,
                               profile: CustomerProfile,
                               config: CustomerConfig,
                               version: Option[Long]
                             )
      extends CustomerCreateCommandRequest {
      override def toEvent: CustomerCreated = CustomerCreated(
        EventId(UUID.randomUUID()),
        entityId,
        status,
        name,
        sexType,
        profile,
        config,
        version
      )
    }

    case class CreateSucceeded(id: CommandResponseId, commandRequestId: CommandRequestId, entityId: CustomerId)
      extends CustomerCommandResponse with CommandSucceeded[CustomerId, Customer]

    case class CreateFailed(id: CommandResponseId, commandRequestId: CommandRequestId, entityId: CustomerId, throwable: Throwable)
      extends CommandFailed[CustomerId]

    trait CustomerCreateEvent extends CustomerEvent with CreateEvent[CustomerId]

    case class CustomerCreated(id: EventId,
                               entityId: CustomerId,
                               status: StatusType.Value,
                               name: String,
                               sexType: SexType.Value,
                               profile: CustomerProfile,
                               config: CustomerConfig,
                               version: Option[Long])
      extends CustomerCreateEvent

  }

  object Update {

    trait CustomerUpdateCommandRequest extends CustomerCommandRequest with EntityProtocol.UpdateCommandRequest[CustomerId] {
      override def toEvent: CustomerUpdateEvent
    }

    case class UpdateName(id: CommandRequestId, entityId: CustomerId, name: String) extends CustomerUpdateCommandRequest {
      override def toEvent: CustomerUpdateEvent = NameUpdated(EventId(UUID.randomUUID()), entityId, name)
    }

    case class UpdateSucceeded(id: CommandResponseId, commandRequestId: CommandRequestId, entityId: CustomerId)
      extends CustomerCommandResponse with CommandSucceeded[CustomerId, Customer]

    case class UpdateFailed(id: CommandResponseId, commandRequestId: CommandRequestId, entityId: CustomerId, throwable: Throwable)
      extends CustomerCommandResponse with CommandFailed[CustomerId]

    trait CustomerUpdateEvent extends CustomerEvent with UpdateEvent[CustomerId]

    case class NameUpdated(id: EventId, entityId: CustomerId, name: String) extends CustomerUpdateEvent

  }

  object Delete {

  }

  object Query {

  }

}


object Customer extends EntityFactory[CustomerId, Customer] {

  override type Event = CustomerCreated

  override def createFromEvent: PartialFunction[CustomerCreated, Customer] = {
    case CustomerCreated(_, customerId, status, name, sexType, profile, config, version) =>
      new Customer(customerId, status, name, sexType, profile, config, version)
  }
}

/**
  * ペットストアの顧客を表すエンティティ。
  *
  * @param id      識別子
  * @param status  [[StatusType]]
  * @param name    名前
  * @param sexType 性別
  * @param profile [[CustomerProfile]]
  * @param config  [[CustomerConfig]]
  */
case class Customer(
                     id: CustomerId,
                     status: StatusType.Value,
                     name: String,
                     sexType: SexType.Value,
                     profile: CustomerProfile,
                     config: CustomerConfig,
                     version: Option[Long]
                   )
  extends BaseEntity[CustomerId] {

  override type This = Customer

  override type Event = CustomerUpdateEvent

  def withName(value: String): Customer =
    copy(name = value)

  override def withVersion(version: Long): Entity[CustomerId] = copy(version = Some(version))

  override def updateState: StateMachine = {
    case NameUpdated(_, entityId, value) =>
      require(entityId == id)
      withName(value)
  }

}

