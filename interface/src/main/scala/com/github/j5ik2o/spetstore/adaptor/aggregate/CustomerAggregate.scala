package com.github.j5ik2o.spetstore.adaptor.aggregate

import akka.actor.Props
import com.github.j5ik2o.spetstore.adaptor.eventbus.EventBus
import com.github.j5ik2o.spetstore.domain.customer._
import com.github.j5ik2o.spetstore.infrastructure.domainsupport._

object CustomerAggregate {

  def name(id: CustomerId): String = s"customer-${id.value}"

  def props(eventBus: EventBus, id: CustomerId): Props = Props(new CustomerAggregate(eventBus, id))

}

final class CustomerAggregate(eventBus: EventBus, id: CustomerId) extends AbstractAggregate[CustomerId, Customer](eventBus, id, CustomerAggregate.name) {

  override protected val entityFactory: EntityFactory[CustomerId, Customer] = Customer

  override def createSucceeded(commandRequest: CommandRequest[CustomerId]): CommandSucceeded[CustomerId, Customer] =
    CustomerCommandResponse.CreateSucceeded(CommandResponseId(), commandRequest.id, state.get)

  override def createFailed(commandRequest: CommandRequest[CustomerId]): CommandFailed =
    CustomerCommandResponse.CreateFailed(CommandResponseId(), commandRequest.id, CreateFailedException("Creating state is failed."))

  override def updateSucceeded(commandRequest: CommandRequest[CustomerId]): CommandSucceeded[CustomerId, Customer] =
    CustomerCommandResponse.UpdateSucceeded(CommandResponseId(), commandRequest.id, state.get)

  override def updateFailed(commandRequest: CommandRequest[CustomerId]): CommandFailed =
    CustomerCommandResponse.UpdateFailed(CommandResponseId(), commandRequest.id, UpdateFailedException("Updating state is failed."))

  override def getSucceeded(commandRequest: CommandRequest[CustomerId]): CommandSucceeded[CustomerId, Customer] =
    CustomerCommandResponse.GetSucceeded(CommandResponseId(), commandRequest.id, state.get)

  override def getFailed(commandRequest: CommandRequest[CustomerId]): CommandFailed =
    CustomerCommandResponse.GetFailed(CommandResponseId(), commandRequest.id, GetFailedException("Getting state is failed."))


  override def receiveRecover: Receive = {
    case event: CustomerCreateEvent => createState(event)
    case event: CustomerUpdateEvent => updateState(event)
  }

  override def receiveCommand: Receive = {
    case commandRequest: CustomerGetCommandRequest => getState(commandRequest)
    case commandRequest: CustomerCreateCommandRequest => createState(commandRequest)
    case commandRequest: CustomerUpdateCommandRequest => updateState(commandRequest)
  }

}
