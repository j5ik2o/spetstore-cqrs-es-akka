package com.github.j5ik2o.spetstore.adaptor.aggregate

import akka.actor.Props
import com.github.j5ik2o.spetstore.adaptor.eventbus.EventBus
import com.github.j5ik2o.spetstore.domain.item.SupplierAggregateProtocol.Query.{ GetStateRequest, GetStateResponse }
import com.github.j5ik2o.spetstore.domain.item.SupplierAggregateProtocol.Create.{ SupplierCreateCommandRequest, SupplierCreateEvent }
import com.github.j5ik2o.spetstore.domain.item.SupplierAggregateProtocol.Update.{ SupplierUpdateCommandRequest, SupplierUpdateEvent }
import com.github.j5ik2o.spetstore.domain.item.{ Supplier, SupplierId }
import com.github.j5ik2o.spetstore.infrastructure.domainsupport.EntityProtocol
import com.github.j5ik2o.spetstore.infrastructure.domainsupport.EntityProtocol._

import scala.reflect.ClassTag

object SupplierAggregate {

  def name(id: SupplierId): String = s"supplier-${id.value}"

  def props(eventBus: EventBus, id: SupplierId): Props = Props(new SupplierAggregate(eventBus, id))
}

class SupplierAggregate(eventBus: EventBus, id: SupplierId)
    extends AbstractAggregate[SupplierId, Supplier, SupplierCreateEvent, SupplierUpdateEvent](eventBus, id, SupplierAggregate.name) {

  override protected val entityFactory = Supplier

  override def getSucceeded[Q <: EntityProtocol.GetStateRequest[SupplierId]: ClassTag](queryRequest: Q): GetStateResponse = ???

  override def updateSucceeded[C <: CommandRequest[SupplierId]](commandRequest: C): CommandSucceeded[SupplierId, Supplier] = ???

  override def updateFailed[C <: CommandRequest[SupplierId]](commandRequest: C): CommandFailed[SupplierId] = ???

  override def createSucceeded[C <: CommandRequest[SupplierId]: ClassTag](commandRequest: C): CommandSucceeded[SupplierId, Supplier] = ???

  override def createFailed[C <: CommandRequest[SupplierId]: ClassTag](commandRequest: C): CommandFailed[SupplierId] = ???

  override def receiveRecover: Receive = {
    case event: SupplierCreateEvent => applyCreateEvent(event)
    case event: SupplierUpdateEvent => applyUpdateEvent(event)
  }

  override def receiveCommand: Receive = {
    case queryRequest: GetStateRequest               => getState(queryRequest)
    case createRequest: SupplierCreateCommandRequest => createState(createRequest)
    case updateRequest: SupplierUpdateCommandRequest => updateState(updateRequest)
  }
}
