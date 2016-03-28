package com.github.j5ik2o.spetstore.domain.customer

import java.util.UUID

import com.github.j5ik2o.spetstore.domain.basic.{SexType, StatusType}
import com.github.j5ik2o.spetstore.infrastructure.domainsupport._

sealed trait CustomerCommandRequest extends CommandRequest[CustomerId] {

}

trait CustomerCreateCommandRequest extends CustomerCommandRequest with CreateCommandRequest[CustomerId] {
  override def toEvent: CustomerCreateEvent
}

trait CustomerUpdateCommandRequest extends CustomerCommandRequest with UpdateCommandRequest[CustomerId] {
  override def toEvent: CustomerUpdateEvent
}

trait CustomerGetCommandRequest extends CustomerCommandRequest with GetCommandRequest[CustomerId]

object CustomerCommandRequest {

  case class CreateCustomer(id: CommandRequestId,
                            entityId: CustomerId,
                            status: StatusType.Value,
                            name: String,
                            sexType: SexType.Value,
                            profile: CustomerProfile,
                            config: CustomerConfig,
                            version: Option[Long])
    extends CustomerCreateCommandRequest {
    override def toEvent: CustomerEvent.CustomerCreated = CustomerEvent.CustomerCreated(
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

  case class UpdateName(id: CommandRequestId, entityId: CustomerId, name: String) extends CustomerUpdateCommandRequest {
    override def toEvent: CustomerUpdateEvent = CustomerEvent.NameUpdated(EventId(UUID.randomUUID()), name)
  }

  case class GetCustomer(id: CommandRequestId, entityId: CustomerId) extends CustomerGetCommandRequest

}

trait CustomerCommandResponse extends CommandResponse

object CustomerCommandResponse {

  case class CreateSucceeded(id: CommandResponseId, commandRequestId: CommandRequestId, entity: Customer)
    extends CustomerCommandResponse with CommandSucceeded[CustomerId, Customer]

  case class CreateFailed(id: CommandResponseId, commandRequestId: CommandRequestId, throwable: Throwable)
    extends CommandFailed

  case class UpdateSucceeded(id: CommandResponseId, commandRequestId: CommandRequestId, entity: Customer)
    extends CustomerCommandResponse with CommandSucceeded[CustomerId, Customer]

  case class UpdateFailed(id: CommandResponseId, commandRequestId: CommandRequestId, throwable: Throwable)
    extends CustomerCommandResponse with CommandFailed

  case class GetSucceeded(id: CommandResponseId, commandRequestId: CommandRequestId, entity: Customer)
    extends CustomerCommandResponse with CommandSucceeded[CustomerId, Customer]

  case class GetFailed(id: CommandResponseId, commandRequestId: CommandRequestId, throwable: Throwable)
    extends CustomerCommandResponse with CommandFailed

}


sealed trait CustomerEvent extends Event

trait CustomerCreateEvent extends CustomerEvent with CreateEvent

trait CustomerUpdateEvent extends CustomerEvent with UpdateEvent

object CustomerEvent {

  case class NameUpdated(id: EventId, name: String) extends CustomerUpdateEvent

  case class CustomerCreated(id: EventId, customerId: CustomerId,
                             status: StatusType.Value,
                             name: String,
                             sexType: SexType.Value,
                             profile: CustomerProfile,
                             config: CustomerConfig,
                             version: Option[Long])
    extends CustomerCreateEvent

}

object Customer extends EntityFactory[CustomerId, Customer] {
  override def createFromEvent: PartialFunction[CreateEvent, Customer] = {
    case CustomerEvent.CustomerCreated(_, customerId, status, name, sexType, profile, config, version) =>
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
case class Customer
(id: CustomerId,
 status: StatusType.Value,
 name: String,
 sexType: SexType.Value,
 profile: CustomerProfile,
 config: CustomerConfig,
 version: Option[Long])
  extends BaseEntity[CustomerId] {

  override type This = Customer

  override type Event = CustomerUpdateEvent

  def withName(value: String): Customer =
    copy(name = value)

  override def withVersion(version: Long): Entity[CustomerId] = copy(version = Some(version))

  override def updateState: StateMachine = {
    case CustomerEvent.NameUpdated(_, value) => withName(value)
  }

}




