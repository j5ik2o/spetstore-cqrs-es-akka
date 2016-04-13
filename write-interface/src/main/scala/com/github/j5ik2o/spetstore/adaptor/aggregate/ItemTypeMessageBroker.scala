package com.github.j5ik2o.spetstore.adaptor.aggregate

import akka.actor.Props
import akka.cluster.sharding.ShardRegion.{ ExtractEntityId, ExtractShardId }
import com.github.j5ik2o.spetstore.adaptor.eventbus.EventBus
import com.github.j5ik2o.spetstore.domain.item.ItemTypeAggregateProtocol.ItemTypeCommandRequest
import com.github.j5ik2o.spetstore.domain.item.ItemTypeId

final class ItemTypeMessageBroker(eventBus: EventBus)
    extends AbstractMessageBroker[ItemTypeId, ItemTypeCommandRequest] with PassivationSupport {

  override def createChildProps(aggregateId: ItemTypeId): Props =
    ItemTypeAggregate.props(eventBus, aggregateId)

  override def getChildName(commandRequest: ItemTypeCommandRequest): String =
    ItemTypeAggregate.name(commandRequest.entityId)

  override def receive: Receive = {
    case commandRequest: ItemTypeCommandRequest => forwardMessage(commandRequest)
  }

}

object ItemTypeMessageBroker extends ShardRegionFactory[ItemTypeId, ItemTypeCommandRequest] {

  override val typeName: String = "ItemType"

  override def props(eventBus: EventBus): Props = Props(new ItemTypeMessageBroker(eventBus))

  override val extractEntityId: ExtractEntityId = {
    case msg: ItemTypeCommandRequest => (entryKey(msg.entityId.value.toString), msg)
  }

  override val extractShardId: ExtractShardId = {
    case msg: ItemTypeCommandRequest => shardKey(msg.entityId.value.toString)
  }

}
