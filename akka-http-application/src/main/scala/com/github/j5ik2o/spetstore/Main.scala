package com.github.j5ik2o.spetstore

import akka.actor.{ ActorPath, ActorSystem }
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.{ ActorMaterializer, Materializer }
import akka.util.Timeout
import com.github.j5ik2o.spetstore.adaptor.aggregate.CustomerMessageBroker
import com.github.j5ik2o.spetstore.adaptor.eventbus.EventBus
import com.github.j5ik2o.spetstore.usecase.CustomerUseCase
import com.typesafe.config.{ Config, ConfigFactory }

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object Main extends App with Route with SharedJournalSupport {
  val httpInterface = "0.0.0.0"
  val httpPort = 8080

  val configuration = ConfigFactory.parseString("akka.remote.netty.tcp.port = " + 2551)
    .withFallback(ConfigFactory.load())

  val clusterPort = configuration.getInt("akka.remote.netty.tcp.port")

  private implicit val actorSystem = ActorSystem("ClusterSystem", configuration)

  override implicit val executor: ExecutionContext = actorSystem.dispatcher

  override implicit val materializer: Materializer = ActorMaterializer()

  startupSharedJournal(
    startStore = clusterPort == 2551,
    path       = ActorPath.fromString("akka.tcp://ClusterSystem@127.0.0.1:2551/user/store")
  )

  val eventBus = EventBus.ofRemote(actorSystem)

  val customerAggregate = CustomerMessageBroker(eventBus)

  override val customerUseCase: CustomerUseCase = CustomerUseCase(customerAggregate)

  Http().bindAndHandle(
    handler   = logRequestResult("log")(routes),
    interface = httpInterface,
    port      = httpPort
  )
}
