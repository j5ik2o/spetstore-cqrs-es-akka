package com.github.j5ik2o.spetstore.adaptor.aggregate

import akka.actor.Props
import com.github.j5ik2o.spetstore.adaptor.eventbus.EventBus
import com.github.j5ik2o.spetstore.domain.item._
import com.github.j5ik2o.spetstore.infrastructure.domainsupport._

object CategoryAggregate {

  def name(id: CategoryId): String = s"category-${id.value}"

  def props(eventBus: EventBus, id: CategoryId): Props = Props(new CategoryAggregate(eventBus, id))

}

final class CategoryAggregate(eventBus: EventBus, id: CategoryId) extends AbstractAggregate[CategoryId, Category](eventBus, id, CategoryAggregate.name) {

  override protected val entityFactory: EntityFactory[CategoryId, Category] = Category

  override def createSucceeded(commandRequest: CommandRequest[CategoryId]): CommandSucceeded[CategoryId, Category] =
    CategoryCommandResponse.CreateSucceeded(CommandResponseId(), commandRequest.id, state.get)

  override def createFailed(commandRequest: CommandRequest[CategoryId]): CommandFailed =
    CategoryCommandResponse.CreateFailed(CommandResponseId(), commandRequest.id, CreateFailedException("Creating state is failed."))

  override def updateSucceeded(commandRequest: CommandRequest[CategoryId]): CommandSucceeded[CategoryId, Category] =
    CategoryCommandResponse.UpdateSucceeded(CommandResponseId(), commandRequest.id, state.get)

  override def updateFailed(commandRequest: CommandRequest[CategoryId]): CommandFailed =
    CategoryCommandResponse.UpdateFailed(CommandResponseId(), commandRequest.id, UpdateFailedException("Creating state is failed."))

  override def getSucceeded(commandRequest: CommandRequest[CategoryId]): CommandSucceeded[CategoryId, Category] =
    CategoryCommandResponse.GetSucceeded(CommandResponseId(), commandRequest.id, state.get)

  override def getFailed(commandRequest: CommandRequest[CategoryId]): CommandFailed =
    CategoryCommandResponse.GetFailed(CommandResponseId(), commandRequest.id, UpdateFailedException("Getting state is failed."))



  override def receiveRecover: Receive = {
    case event: CategoryCreateEvent => createState(event)
    case event: CategoryUpdateEvent => updateState(event)
  }

  override def receiveCommand: Receive = {
    case commandRequest: CategoryGetCommandRequest => getState(commandRequest)
    case commandRequest: CategoryCreateCommandRequest => createState(commandRequest)
    case commandRequest: CategoryUpdateCommandRequest => updateState(commandRequest)
  }

}
