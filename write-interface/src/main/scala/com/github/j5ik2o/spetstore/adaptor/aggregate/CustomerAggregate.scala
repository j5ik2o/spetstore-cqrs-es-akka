package com.github.j5ik2o.spetstore.adaptor.aggregate

import akka.actor.Props
import com.github.j5ik2o.spetstore.adaptor.eventbus.EventBus
import com.github.j5ik2o.spetstore.domain.customer.CustomerAggregateProtocol.Create._
import com.github.j5ik2o.spetstore.domain.customer.CustomerAggregateProtocol.Query.{ GetStateRequest, GetStateResponse }
import com.github.j5ik2o.spetstore.domain.customer.CustomerAggregateProtocol.Update.{ CustomerUpdateCommandRequest, CustomerUpdateEvent, UpdateFailed, UpdateSucceeded }
import com.github.j5ik2o.spetstore.domain.customer.{ Customer, CustomerId }
import com.github.j5ik2o.spetstore.infrastructure.domainsupport.EntityProtocol._
import com.github.j5ik2o.spetstore.infrastructure.domainsupport._

import scala.reflect.ClassTag

object CustomerAggregate {

  def name(id: CustomerId): String = s"customer-${id.value}"

  def props(eventBus: EventBus, id: CustomerId): Props = Props(new CustomerAggregate(eventBus, id))

}

final class CustomerAggregate(eventBus: EventBus, id: CustomerId)
    extends AbstractAggregate[CustomerId, Customer, CustomerCreateEvent, CustomerUpdateEvent](eventBus, id, CustomerAggregate.name) {

  override protected val entityFactory = Customer

  override def getSucceeded[Q <: EntityProtocol.GetStateRequest[CustomerId]: ClassTag](queryRequest: Q): GetStateResponse =
    GetStateResponse(QueryResponseId(), queryRequest.id, id, state)

  override def createSucceeded[C <: EntityProtocol.CommandRequest[CustomerId]: ClassTag](commandRequest: C): EntityProtocol.CommandSucceeded[CustomerId, Customer] =
    CreateSucceeded(CommandResponseId(), commandRequest.id, commandRequest.entityId)

  override def createFailed[C <: EntityProtocol.CommandRequest[CustomerId]: ClassTag](commandRequest: C): EntityProtocol.CommandFailed[CustomerId] =
    CreateFailed(CommandResponseId(), commandRequest.id, commandRequest.entityId, new Exception)

  override def updateSucceeded[C <: EntityProtocol.CommandRequest[CustomerId]](commandRequest: C): EntityProtocol.CommandSucceeded[CustomerId, Customer] =
    UpdateSucceeded(CommandResponseId(), commandRequest.id, commandRequest.entityId)

  override def updateFailed[C <: EntityProtocol.CommandRequest[CustomerId]](commandRequest: C): EntityProtocol.CommandFailed[CustomerId] =
    UpdateFailed(CommandResponseId(), commandRequest.id, commandRequest.entityId, new Exception)

  override def receiveRecover: Receive = {
    case createEvent: CustomerCreateEvent => applyCreateEvent(createEvent)
    case updateEvent: CustomerUpdateEvent => applyUpdateEvent(updateEvent)
  }

  override def receiveCommand: Receive = {
    case queryRequest: GetStateRequest               => getState(queryRequest)
    case createRequest: CustomerCreateCommandRequest => createState(createRequest)
    case updateRequest: CustomerUpdateCommandRequest => updateState(updateRequest)
  }

}
