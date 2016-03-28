package com.github.j5ik2o.spetstore.adaptor.eventbus

import akka.actor.{ActorRef, ActorSystem}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Publish, Subscribe, Unsubscribe}
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

    val m = DistributedPubSub(system).mediator

    val topics = scala.collection.mutable.Map.empty[ActorRef, Class[_]]

    override def subscribe(subscriber: ActorRef, to: Class[_]): Boolean = {
      logger.debug(s">>>>>> subscribe = $subscriber, $to")
      topics += (subscriber -> to)
      m ! Subscribe(to.toString, subscriber)
      true
    }

    override def publish(eventWithSender: EventWithSender): Unit = {
      m ! Publish(eventWithSender.event.getClass.toString, eventWithSender)
      logger.debug(s">>>>>> publish = $eventWithSender")
    }

    override def unsubscribe(subscriber: ActorRef, from: Class[_]): Boolean = {
      if (topics.contains(subscriber)) {
        m ! Unsubscribe(from.toString, subscriber)
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

    private val subscribers = scala.collection.mutable.Map.empty[Classifier, List[ActorRef]]

    override def subscribe(subscriber: ActorRef, to: Classifier): Boolean = {
      subscribers += (to -> (subscriber :: subscribers.getOrElse(to, Nil)))
      true
    }

    override def publish(eventWithSender: EventWithSender): Unit = {
      subscribers.filter {
        case (k, _) =>
          val result = k.isAssignableFrom(eventWithSender.event.getClass)
          result
      }.foreach {
        case (_, v) =>
          v.foreach { ref =>
            ref ! eventWithSender
          }
      }
    }

    override def unsubscribe(subscriber: ActorRef, from: Classifier): Boolean = {
      subscribers.remove(from).isDefined
    }

    override def unsubscribe(subscriber: ActorRef): Unit = {
      val newMap = subscribers.map { case (k, v) => (k, v.filterNot(_ == subscriber)) }
      subscribers ++= newMap
    }
  }

  def ofLocal(system: ActorSystem): EventBus = new EventBusOnLocal(system)
  def ofRemote(system: ActorSystem): EventBus = new EventBusOnRemote(system)

}