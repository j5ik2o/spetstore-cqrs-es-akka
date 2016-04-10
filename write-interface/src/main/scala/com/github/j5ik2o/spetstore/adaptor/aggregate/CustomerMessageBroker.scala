package com.github.j5ik2o.spetstore.adaptor.aggregate

import akka.actor.Props
import akka.cluster.sharding.ShardRegion._
import com.github.j5ik2o.spetstore.adaptor.eventbus.EventBus
import com.github.j5ik2o.spetstore.domain.customer.CustomerAggregateProtocol.CustomerCommandRequest
import com.github.j5ik2o.spetstore.domain.customer.CustomerId

class CustomerMessageBroker(eventBus: EventBus)
  extends AbstractMessageBroker[CustomerId, CustomerCommandRequest] {

  override def createChildProps(aggregateId: CustomerId): Props =
    CustomerAggregate.props(eventBus, aggregateId)

  override def getChildName(commandRequest: CustomerCommandRequest): String =
    CustomerAggregate.name(commandRequest.entityId)

  override def receive: Receive = {
    case commandRequest: CustomerCommandRequest => forwardMessage(commandRequest)
  }
}

object CustomerMessageBroker extends ShardRegionFactory[CustomerId, CustomerCommandRequest] {

  override val typeName: String = "Customer"

  override def props(eventBus: EventBus): Props = Props(new CustomerMessageBroker(eventBus))

  override val extractEntityId: ExtractEntityId = {
    case msg: CustomerCommandRequest => (entryKey(msg.entityId.value.toString), msg)
  }

  override val extractShardId: ExtractShardId = {
    case msg: CustomerCommandRequest => shardKey(msg.entityId.value.toString)
  }

}
