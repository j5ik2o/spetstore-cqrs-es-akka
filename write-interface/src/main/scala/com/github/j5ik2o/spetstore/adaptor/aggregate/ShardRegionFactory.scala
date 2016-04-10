package com.github.j5ik2o.spetstore.adaptor.aggregate

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.cluster.sharding.{ ClusterSharding, ClusterShardingSettings, ShardRegion }
import com.github.j5ik2o.spetstore.adaptor.eventbus.EventBus
import com.github.j5ik2o.spetstore.infrastructure.domainsupport.EntityId
import com.github.j5ik2o.spetstore.infrastructure.domainsupport.EntityProtocol.CommandRequest

trait ShardRegionFactory[ID <: EntityId, CR <: CommandRequest[ID]] {

  val typeName: String

  val numOfShards = 8

  val entriesPerShard = 8

  protected def shardKey(id: String) = Math.abs(id.hashCode % numOfShards).toString

  protected def entryKey(id: String) = Math.abs(id.hashCode % (numOfShards * entriesPerShard)).toString

  val extractEntityId: ShardRegion.ExtractEntityId

  val extractShardId: ShardRegion.ExtractShardId

  def props(eventBus: EventBus): Props

  def resolve(implicit system: ActorSystem): ActorRef =
    ClusterSharding(system).shardRegion(typeName)

  def apply(eventBus: EventBus)(implicit system: ActorSystem): ActorRef = {
    ClusterSharding(system).start(
      typeName,
      props(eventBus),
      ClusterShardingSettings(system),
      extractEntityId,
      extractShardId
    )
  }

}
