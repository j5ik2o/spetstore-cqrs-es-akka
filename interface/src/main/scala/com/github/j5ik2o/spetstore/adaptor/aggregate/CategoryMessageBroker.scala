package com.github.j5ik2o.spetstore.adaptor.aggregate

import akka.actor.Props
import akka.cluster.sharding.ShardRegion.{ExtractEntityId, ExtractShardId}
import com.github.j5ik2o.spetstore.adaptor.eventbus.EventBus
import com.github.j5ik2o.spetstore.domain.item.{CategoryCommandRequest, CategoryId}

final class CategoryMessageBroker(eventBus: EventBus) extends AbstractMessageBroker[CategoryId, CategoryCommandRequest] {

  override def createChildProps(aggregateId: CategoryId): Props = CategoryAggregate.props(eventBus, aggregateId)

  override def getChildName(commandRequest: CategoryCommandRequest): String =
    CategoryAggregate.name(commandRequest.entityId)

  override def receive: Receive = {
    case commandRequest: CategoryCommandRequest => forwardMessage(commandRequest)
  }

}

object CategoryMessageBroker extends ShardRegionFactory[CategoryId, CategoryCommandRequest] {

  override val typeName: String = "Category"

  override def props(eventBus: EventBus): Props = Props(new CategoryMessageBroker(eventBus))

  override val extractEntityId: ExtractEntityId = {
    case msg: CategoryCommandRequest => (entryKey(msg.entityId.value.toString), msg)
  }

  override val extractShardId: ExtractShardId = {
    case msg: CategoryCommandRequest => shardKey(msg.entityId.value.toString)
  }

}
