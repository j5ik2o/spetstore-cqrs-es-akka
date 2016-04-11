package com.github.j5ik2o.spetstore.adaptor.aggregate

import akka.actor.Props
import com.github.j5ik2o.spetstore.adaptor.eventbus.EventBus
import com.github.j5ik2o.spetstore.domain.purchase.CartAggregateProtocol.Query.{ GetStateRequest, GetStateResponse }
import com.github.j5ik2o.spetstore.domain.purchase.CartAggregateProtocol.Create.{ CartCreateCommandRequest, CartCreateEvent, CreateFailed, CreateSucceeded }
import com.github.j5ik2o.spetstore.domain.purchase.CartAggregateProtocol.Update.{ CartUpdateCommandRequest, CartUpdateEvent, UpdateFailed, UpdateSucceeded }
import com.github.j5ik2o.spetstore.domain.purchase.{ Cart, CartId }
import com.github.j5ik2o.spetstore.infrastructure.domainsupport.{ EntityFactory, EntityProtocol }
import com.github.j5ik2o.spetstore.infrastructure.domainsupport.EntityProtocol._

import scala.reflect.ClassTag

object CartAggregate {

  def name(id: CartId): String = s"cart-${id.value}"

  def props(eventBus: EventBus, id: CartId): Props = Props(new CartAggregate(eventBus, id))

}

final class CartAggregate(eventBus: EventBus, id: CartId)
    extends AbstractAggregate[CartId, Cart, CartCreateEvent, CartUpdateEvent](eventBus, id, CartAggregate.name) {
  override protected val entityFactory: EntityFactory[CartId, Cart, CartCreateEvent, CartUpdateEvent] = Cart

  override def getSucceeded[Q <: EntityProtocol.GetStateRequest[CartId]: ClassTag](queryRequest: Q): GetStateResponse =
    GetStateResponse(QueryResponseId(), queryRequest.id, id, state)

  override def createSucceeded[C <: EntityProtocol.CommandRequest[CartId]: ClassTag](commandRequest: C): CommandSucceeded[CartId, Cart] =
    CreateSucceeded(CommandResponseId(), commandRequest.id, id)

  override def createFailed[C <: EntityProtocol.CommandRequest[CartId]: ClassTag](commandRequest: C): CommandFailed[CartId] =
    CreateFailed(CommandResponseId(), commandRequest.id, id, new Exception)

  override def updateSucceeded[C <: EntityProtocol.CommandRequest[CartId]](commandRequest: C): CommandSucceeded[CartId, Cart] =
    UpdateSucceeded(CommandResponseId(), commandRequest.id, id)

  override def updateFailed[C <: EntityProtocol.CommandRequest[CartId]](commandRequest: C): CommandFailed[CartId] =
    UpdateFailed(CommandResponseId(), commandRequest.id, id, new Exception)

  override def receiveRecover: Receive = {
    case createEvent: CartCreateEvent => applyCreateEvent(createEvent)
    case updateEvent: CartUpdateEvent => applyUpdateEvent(updateEvent)
  }

  override def receiveCommand: Receive = {
    case queryRequest: GetStateRequest           => getState(queryRequest)
    case updateRequest: CartUpdateCommandRequest => updateState(updateRequest)
    case createRequest: CartCreateCommandRequest => createState(createRequest)
  }
}
