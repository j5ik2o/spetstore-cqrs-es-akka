package com.github.j5ik2o.spetstore.adaptor.aggregate

import akka.actor.{ ReceiveTimeout, Actor }
import akka.cluster.sharding.ShardRegion.Passivate

object PassivationSupportProtocol {
  case object StopWriting
}

trait PassivationSupport extends Actor {
  import PassivationSupportProtocol._

  override def unhandled(msg: Any): Unit = msg match {
    case ReceiveTimeout =>
      context.parent ! Passivate(stopMessage = StopWriting)
    case StopWriting => context.stop(self)
    case other       => super.unhandled(other)
  }
}
