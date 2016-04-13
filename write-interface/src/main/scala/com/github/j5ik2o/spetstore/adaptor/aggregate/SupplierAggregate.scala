package com.github.j5ik2o.spetstore.adaptor.aggregate

import akka.actor.Props
import com.github.j5ik2o.spetstore.adaptor.eventbus.EventBus
import com.github.j5ik2o.spetstore.domain.item.SupplierAggregateProtocol.Create.{CreateFailed, CreateSucceeded, SupplierCreateCommandRequest, SupplierCreateEvent}
import com.github.j5ik2o.spetstore.domain.item.SupplierAggregateProtocol.Query.{GetStateRequest, GetStateResponse}
import com.github.j5ik2o.spetstore.domain.item.SupplierAggregateProtocol.Update.{SupplierUpdateCommandRequest, SupplierUpdateEvent, UpdateFailed, UpdateSucceeded}
import com.github.j5ik2o.spetstore.domain.item.{Supplier, SupplierId}
import com.github.j5ik2o.spetstore.infrastructure.domainsupport.EntityProtocol
import com.github.j5ik2o.spetstore.infrastructure.domainsupport.EntityProtocol._

import scala.reflect.ClassTag

object SupplierAggregate {

  def name(id: SupplierId): String = s"supplier-${id.value}"

  def props(eventBus: EventBus, id: SupplierId): Props = Props(new SupplierAggregate(eventBus, id))
}

final class SupplierAggregate(eventBus: EventBus, id: SupplierId)
  extends AbstractAggregate[SupplierId, Supplier, SupplierCreateEvent, SupplierUpdateEvent](eventBus, id, SupplierAggregate.name) {

  override protected val entityFactory = Supplier

  override def getSucceeded[Q <: EntityProtocol.GetStateRequest[SupplierId] : ClassTag](queryRequest: Q): GetStateResponse =
    GetStateResponse(QueryResponseId(), queryRequest.id, id, state)

  override def createSucceeded[C <: CommandRequest[SupplierId] : ClassTag](commandRequest: C): CommandSucceeded[SupplierId, Supplier] =
    CreateSucceeded(CommandResponseId(), commandRequest.id, id)

  override def createFailed[C <: CommandRequest[SupplierId] : ClassTag](commandRequest: C): CommandFailed[SupplierId] =
    CreateFailed(CommandResponseId(), commandRequest.id, id, new Exception)

  override def updateSucceeded[C <: CommandRequest[SupplierId] : ClassTag](commandRequest: C): CommandSucceeded[SupplierId, Supplier] =
    UpdateSucceeded(CommandResponseId(), commandRequest.id, id)

  override def updateFailed[C <: CommandRequest[SupplierId] : ClassTag](commandRequest: C): CommandFailed[SupplierId] =
    UpdateFailed(CommandResponseId(), commandRequest.id, id, new Exception)

  override def receiveRecover: Receive = {
    case event: SupplierCreateEvent => applyCreateEvent(event)
    case event: SupplierUpdateEvent => applyUpdateEvent(event)
  }

  override def receiveCommand: Receive = {
    case queryRequest: GetStateRequest => getState(queryRequest)
    case createRequest: SupplierCreateCommandRequest => createState(createRequest)
    case updateRequest: SupplierUpdateCommandRequest => updateState(updateRequest)
  }

}
