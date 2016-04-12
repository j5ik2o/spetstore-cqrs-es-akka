import javax.inject.Inject

import akka.actor.{ ActorRef, ActorSystem }
import akka.stream.Materializer
import com.github.j5ik2o.spetstore.adaptor.aggregate.CustomerMessageBroker
import com.github.j5ik2o.spetstore.adaptor.eventbus.EventBus
import com.google.inject.name.Names
import com.google.inject.{ AbstractModule, Provider }
import play.api.libs.concurrent.AkkaGuiceSupport

class EventBussProvider @Inject() (actorSystem: ActorSystem, materializer: Materializer) extends Provider[EventBus] {
  override def get(): EventBus = {
    EventBus.ofLocal(actorSystem)
  }
}

class CustomerAggregateProvider @Inject() (actorSystem: ActorSystem, eventBus: EventBus) extends Provider[ActorRef] {
  override def get(): ActorRef = {
    actorSystem.actorOf(CustomerMessageBroker.props(eventBus))
  }
}

class Module extends AbstractModule with AkkaGuiceSupport {

  override def configure() = {
    bind(classOf[EventBus])
      .toProvider(classOf[EventBussProvider])
      .asEagerSingleton()
    bind(classOf[ActorRef])
      .annotatedWith(Names.named("customer-aggregate"))
      .toProvider(classOf[CustomerAggregateProvider])
      .asEagerSingleton()
  }

}
