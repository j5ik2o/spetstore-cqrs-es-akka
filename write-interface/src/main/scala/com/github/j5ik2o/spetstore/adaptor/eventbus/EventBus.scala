package com.github.j5ik2o.spetstore.adaptor.eventbus

import akka.actor.{ ActorRef, ActorSystem }
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{ Publish, Subscribe, Unsubscribe }
import akka.event.ActorEventBus
import org.slf4j.LoggerFactory

case class EventWithSender(event: Any, sender: ActorRef)

trait EventBus extends ActorEventBus {
  type Event = EventWithSender
  type Classifier = Class[_]
}

object EventBus {

  class EventBusOnRemote(system: ActorSystem) extends EventBus {

    val logger = LoggerFactory.getLogger(getClass)

    val mediator = DistributedPubSub(system).mediator

    val topics = scala.collection.mutable.Map.empty[ActorRef, Class[_]]

    override def subscribe(subscriber: ActorRef, to: Class[_]): Boolean = {
      topics += (subscriber -> to)
      mediator ! Subscribe(to.toString, subscriber)
      true
    }

    override def publish(eventWithSender: EventWithSender): Unit = {
      mediator ! Publish(eventWithSender.event.getClass.toString, eventWithSender)
    }

    override def unsubscribe(subscriber: ActorRef, from: Class[_]): Boolean = {
      if (topics.contains(subscriber)) {
        mediator ! Unsubscribe(from.toString, subscriber)
        topics -= subscriber
        true
      } else false
    }

    override def unsubscribe(subscriber: ActorRef): Unit = {
      topics.filter {
        case (key, _) =>
          key == subscriber
      }.foreach {
        case (key, value) =>
          unsubscribe(key, value)
      }
    }

  }

  class EventBusOnLocal(val system: ActorSystem) extends EventBus {

    private val eventStream = system.eventStream

    override def subscribe(subscriber: ActorRef, to: Classifier): Boolean = {
      eventStream.subscribe(subscriber, to)
    }

    override def publish(eventWithSender: EventWithSender): Unit = {
      eventStream.publish(eventWithSender)
    }

    override def unsubscribe(subscriber: ActorRef, from: Classifier): Boolean = {
      eventStream.unsubscribe(subscriber, from)
    }

    override def unsubscribe(subscriber: ActorRef): Unit = {
      eventStream.unsubscribe(subscriber)
    }
  }

  def ofLocal(system: ActorSystem): EventBus = new EventBusOnLocal(system)

  def ofRemote(system: ActorSystem): EventBus = new EventBusOnRemote(system)

}