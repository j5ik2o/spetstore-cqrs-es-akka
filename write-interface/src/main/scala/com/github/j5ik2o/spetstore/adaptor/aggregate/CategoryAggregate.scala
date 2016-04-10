package com.github.j5ik2o.spetstore.adaptor.aggregate

import akka.actor.Props
import com.github.j5ik2o.spetstore.adaptor.eventbus.EventBus
import com.github.j5ik2o.spetstore.domain.item.CategoryAggregateProtocol.Create._
import com.github.j5ik2o.spetstore.domain.item.CategoryAggregateProtocol.Query.{GetStateRequest, GetStateResponse}
import com.github.j5ik2o.spetstore.domain.item.CategoryAggregateProtocol.Update.{CategoryUpdateCommandRequest, CategoryUpdateEvent, UpdateFailed, UpdateSucceeded}
import com.github.j5ik2o.spetstore.domain.item._
import com.github.j5ik2o.spetstore.infrastructure.domainsupport.EntityProtocol._
import com.github.j5ik2o.spetstore.infrastructure.domainsupport._

import scala.reflect.ClassTag

object CategoryAggregate {

  def name(id: CategoryId): String = s"category-${id.value}"

  def props(eventBus: EventBus, id: CategoryId): Props = Props(new CategoryAggregate(eventBus, id))

}

final class CategoryAggregate(eventBus: EventBus, id: CategoryId)
  extends AbstractAggregate[CategoryId, Category, CategoryCreateEvent, CategoryUpdateEvent](eventBus, id, CategoryAggregate.name) {

  override protected val entityFactory = Category

  override def getSucceeded[Q <: EntityProtocol.GetStateRequest[CategoryId] : ClassTag](queryRequest: Q): GetStateResponse =
    GetStateResponse(QueryResponseId(), queryRequest.id, id, state)

  override def createSucceeded[C <: CommandRequest[CategoryId] : ClassTag](commandRequest: C): CommandSucceeded[CategoryId, Category] =
    CreateSucceeded(CommandResponseId(), commandRequest.id, commandRequest.entityId)

  override def createFailed[C <: CommandRequest[CategoryId] : ClassTag](commandRequest: C): CommandFailed[CategoryId] =
    CreateFailed(CommandResponseId(), commandRequest.id, commandRequest.entityId, new Exception)

  override def updateSucceeded[C <: CommandRequest[CategoryId]](commandRequest: C): CommandSucceeded[CategoryId, Category] =
    UpdateSucceeded(CommandResponseId(), commandRequest.id, commandRequest.entityId)

  override def updateFailed[C <: CommandRequest[CategoryId]](commandRequest: C): CommandFailed[CategoryId] =
    UpdateFailed(CommandResponseId(), commandRequest.id, commandRequest.entityId, new Exception)

  override def receiveRecover: Receive = {
    case createEvent: CategoryCreateEvent => applyCreateEvent(createEvent)
    case updateEvent: CategoryUpdateEvent => applyUpdateEvent(updateEvent)
  }

  override def receiveCommand: Receive = {
    case queryRequest: GetStateRequest => getState(queryRequest)
    case updateRequest: CategoryUpdateCommandRequest => updateState(updateRequest)
    case createRequest: CategoryCreateCommandRequest => createState(createRequest)
  }

}
