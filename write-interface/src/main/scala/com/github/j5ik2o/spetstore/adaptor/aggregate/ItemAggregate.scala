package com.github.j5ik2o.spetstore.adaptor.aggregate

import akka.actor.Props
import com.github.j5ik2o.spetstore.adaptor.eventbus.EventBus
import com.github.j5ik2o.spetstore.domain.item.ItemAggregateProtocol.Create.{ CreateFailed, CreateSucceeded, ItemCreateCommandRequest, ItemCreateEvent }
import com.github.j5ik2o.spetstore.domain.item.ItemAggregateProtocol.Query.{ GetStateRequest, GetStateResponse }
import com.github.j5ik2o.spetstore.domain.item.ItemAggregateProtocol.Update.{ ItemUpdateCommandRequest, ItemUpdateEvent, UpdateFailed, UpdateSucceeded }
import com.github.j5ik2o.spetstore.domain.item.{ Item, ItemId }
import com.github.j5ik2o.spetstore.infrastructure.domainsupport.EntityProtocol
import com.github.j5ik2o.spetstore.infrastructure.domainsupport.EntityProtocol._

import scala.reflect.ClassTag

object ItemAggregate {
  def name(id: ItemId): String = s"item-${id.value}"

  def props(eventBus: EventBus, id: ItemId): Props = Props(new ItemAggregate(eventBus, id))
}

class ItemAggregate(eventBus: EventBus, id: ItemId)
    extends AbstractAggregate[ItemId, Item, ItemCreateEvent, ItemUpdateEvent](eventBus, id, ItemAggregate.name) {

  override protected val entityFactory = Item

  override def getSucceeded[Q <: EntityProtocol.GetStateRequest[ItemId]: ClassTag](queryRequest: Q): GetStateResponse = {
    GetStateResponse(QueryResponseId(), queryRequest.id, id, state)
  }

  override def createSucceeded[C <: CommandRequest[ItemId]: ClassTag](commandRequest: C): CommandSucceeded[ItemId, Item] =
    CreateSucceeded(CommandResponseId(), commandRequest.id, id)

  override def createFailed[C <: CommandRequest[ItemId]: ClassTag](commandRequest: C): CommandFailed[ItemId] =
    CreateFailed(CommandResponseId(), commandRequest.id, id, new Exception)

  override def updateSucceeded[C <: CommandRequest[ItemId]](commandRequest: C): CommandSucceeded[ItemId, Item] =
    UpdateSucceeded(CommandResponseId(), commandRequest.id, id)

  override def updateFailed[C <: CommandRequest[ItemId]](commandRequest: C): CommandFailed[ItemId] =
    UpdateFailed(CommandResponseId(), commandRequest.id, id, new Exception)

  override def receiveRecover: Receive = {
    case event: ItemCreateEvent => applyCreateEvent(event)
    case event: ItemUpdateEvent => applyUpdateEvent(event)
  }

  override def receiveCommand: Receive = {
    case queryRequest: GetStateRequest           => getState(queryRequest)
    case createRequest: ItemCreateCommandRequest => createState(createRequest)
    case updateRequest: ItemUpdateCommandRequest => updateState(updateRequest)
  }

}
