package com.github.j5ik2o.spetstore.adaptor.aggregate

import akka.actor.Props
import com.github.j5ik2o.spetstore.adaptor.eventbus.EventBus
import com.github.j5ik2o.spetstore.domain.purchase.OrderAggregateProtocol.Create.{ CreateFailed, CreateSucceeded, OrderCreateCommandRequest, OrderCreateEvent }
import com.github.j5ik2o.spetstore.domain.purchase.OrderAggregateProtocol.Query.{ GetStateRequest, GetStateResponse }
import com.github.j5ik2o.spetstore.domain.purchase.OrderAggregateProtocol.Update.{ OrderUpdateCommandRequest, OrderUpdateEvent, UpdateFailed, UpdateSucceeded }
import com.github.j5ik2o.spetstore.domain.purchase.{ Order, OrderId }
import com.github.j5ik2o.spetstore.infrastructure.domainsupport.EntityProtocol
import com.github.j5ik2o.spetstore.infrastructure.domainsupport.EntityProtocol._

import scala.reflect.ClassTag

object OrderAggregate {

  def name(id: OrderId): String = s"order-${id.value}"

  def props(eventBus: EventBus, id: OrderId): Props = Props(new OrderAggregate(eventBus, id))

}

final class OrderAggregate(eventBus: EventBus, id: OrderId) extends AbstractAggregate[OrderId, Order, OrderCreateEvent, OrderUpdateEvent](eventBus, id, OrderAggregate.name) {

  override protected val entityFactory = Order

  override def getSucceeded[Q <: EntityProtocol.GetStateRequest[OrderId]: ClassTag](queryRequest: Q): GetStateResponse =
    GetStateResponse(QueryResponseId(), queryRequest.id, id, state)

  override def createSucceeded[C <: EntityProtocol.CommandRequest[OrderId]: ClassTag](commandRequest: C): CommandSucceeded[OrderId, Order] =
    CreateSucceeded(CommandResponseId(), commandRequest.id, id)

  override def createFailed[C <: EntityProtocol.CommandRequest[OrderId]: ClassTag](commandRequest: C): CommandFailed[OrderId] =
    CreateFailed(CommandResponseId(), commandRequest.id, id, new Exception)

  override def updateSucceeded[C <: EntityProtocol.CommandRequest[OrderId] : ClassTag](commandRequest: C): CommandSucceeded[OrderId, Order] =
    UpdateSucceeded(CommandResponseId(), commandRequest.id, id)

  override def updateFailed[C <: EntityProtocol.CommandRequest[OrderId] : ClassTag](commandRequest: C): CommandFailed[OrderId] =
    UpdateFailed(CommandResponseId(), commandRequest.id, id, new Exception)

  override def receiveRecover: Receive = {
    case event: OrderCreateEvent => applyCreateEvent(event)
    case event: OrderUpdateEvent => applyUpdateEvent(event)
  }

  override def receiveCommand: Receive = {
    case queryRequest: GetStateRequest            => getState(queryRequest)
    case createRequest: OrderCreateCommandRequest => createState(createRequest)
    case updateRequest: OrderUpdateCommandRequest => updateState(updateRequest)
  }
}
