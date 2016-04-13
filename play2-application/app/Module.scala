import javax.inject.Inject

import akka.actor.{ ActorRef, ActorSystem }
import akka.stream.Materializer
import com.github.j5ik2o.spetstore.adaptor.aggregate.{ CustomerMessageBroker, ItemTypeMessageBroker }
import com.github.j5ik2o.spetstore.adaptor.eventbus.EventBus
import com.google.inject.name.Names
import com.google.inject.{ AbstractModule, Provider }
import play.api.libs.concurrent.AkkaGuiceSupport

class EventBussProvider @Inject() (actorSystem: ActorSystem, materializer: Materializer) extends Provider[EventBus] {
  override def get(): EventBus = {
    EventBus.ofRemote(actorSystem)
  }
}

class CustomerAggregateProvider @Inject() (actorSystem: ActorSystem, eventBus: EventBus) extends Provider[ActorRef] {
  override def get(): ActorRef = {
    CustomerMessageBroker(eventBus)(actorSystem)
  }
}

class ItemTypeAggregateProvider @Inject() (actorSystem: ActorSystem, eventBus: EventBus) extends Provider[ActorRef] {
  override def get(): ActorRef = {
    ItemTypeMessageBroker(eventBus)(actorSystem)
  }
}

class Module extends AbstractModule with AkkaGuiceSupport {

  override def configure() = {
    bind(classOf[SharedJournalStarter]).asEagerSingleton()
    bind(classOf[EventBus])
      .toProvider(classOf[EventBussProvider])
      .asEagerSingleton()
    bind(classOf[ActorRef])
      .annotatedWith(Names.named("customer-aggregate"))
      .toProvider(classOf[CustomerAggregateProvider])
      .asEagerSingleton()
    bind(classOf[ActorRef])
      .annotatedWith(Names.named("item-type-aggregate"))
      .toProvider(classOf[ItemTypeAggregateProvider])
      .asEagerSingleton()
  }

}
