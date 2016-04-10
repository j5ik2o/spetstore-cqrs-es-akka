package com.github.j5ik2o.spetstore.adaptor.aggregate

import akka.actor.Props
import com.github.j5ik2o.spetstore.adaptor.eventbus.EventBus
import com.github.j5ik2o.spetstore.domain.item.ItemTypeAggregateProtocol.Create.{ CreateFailed, CreateSucceeded, ItemTypeCreateCommandRequest, ItemTypeCreateEvent }
import com.github.j5ik2o.spetstore.domain.item.ItemTypeAggregateProtocol.Query.{ GetStateRequest, GetStateResponse }
import com.github.j5ik2o.spetstore.domain.item.ItemTypeAggregateProtocol.Update.{ ItemTypeUpdateCommandRequest, ItemTypeUpdateEvent, UpdateFailed, UpdateSucceeded }
import com.github.j5ik2o.spetstore.domain.item.{ ItemType, ItemTypeId }
import com.github.j5ik2o.spetstore.infrastructure.domainsupport.EntityProtocol
import com.github.j5ik2o.spetstore.infrastructure.domainsupport.EntityProtocol.{ CommandResponseId, QueryResponseId }

import scala.reflect.ClassTag

object ItemTypeAggregate {

  def name(id: ItemTypeId): String = s"item-type-${id.value}"

  def props(eventBus: EventBus, id: ItemTypeId): Props = Props(new ItemTypeAggregate(eventBus, id))

}

class ItemTypeAggregate(eventBus: EventBus, id: ItemTypeId)
    extends AbstractAggregate[ItemTypeId, ItemType, ItemTypeCreateEvent, ItemTypeUpdateEvent](eventBus, id, ItemTypeAggregate.name) {

  override protected val entityFactory = ItemType

  override def getSucceeded[Q <: EntityProtocol.GetStateRequest[ItemTypeId]: ClassTag](queryRequest: Q): GetStateResponse =
    GetStateResponse(QueryResponseId(), queryRequest.id, id, state)

  override def createSucceeded[C <: EntityProtocol.CommandRequest[ItemTypeId]: ClassTag](commandRequest: C): EntityProtocol.CommandSucceeded[ItemTypeId, ItemType] =
    CreateSucceeded(CommandResponseId(), commandRequest.id, id)

  override def createFailed[C <: EntityProtocol.CommandRequest[ItemTypeId]: ClassTag](commandRequest: C): EntityProtocol.CommandFailed[ItemTypeId] =
    CreateFailed(CommandResponseId(), commandRequest.id, id, new Exception)

  override def updateSucceeded[C <: EntityProtocol.CommandRequest[ItemTypeId]](commandRequest: C): EntityProtocol.CommandSucceeded[ItemTypeId, ItemType] =
    UpdateSucceeded(CommandResponseId(), commandRequest.id, id)

  override def updateFailed[C <: EntityProtocol.CommandRequest[ItemTypeId]](commandRequest: C): EntityProtocol.CommandFailed[ItemTypeId] =
    UpdateFailed(CommandResponseId(), commandRequest.id, id, new Exception)

  override def receiveRecover: Receive = {
    case event: ItemTypeCreateEvent => applyCreateEvent(event)
    case event: ItemTypeUpdateEvent => applyUpdateEvent(event)
  }

  override def receiveCommand: Receive = {
    case queryRequest: GetStateRequest               => getState(queryRequest)
    case createRequest: ItemTypeCreateCommandRequest => createState(createRequest)
    case updateRequest: ItemTypeUpdateCommandRequest => updateState(updateRequest)
  }

}
