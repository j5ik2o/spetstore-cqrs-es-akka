package com.github.j5ik2o.spetstore

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.{ActorMaterializer, Materializer}
import akka.util.Timeout
import com.github.j5ik2o.spetstore.adaptor.aggregate.CustomerMessageBroker
import com.github.j5ik2o.spetstore.adaptor.eventbus.EventBus
import com.github.j5ik2o.spetstore.adaptor.http.Route
import com.github.j5ik2o.spetstore.usecase.CustomerUseCase

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object Main extends App with Route {
  val httpInterface = "0.0.0.0"
  val httpPort = 8080

  implicit val timeout = Timeout(10 seconds)

  implicit val actorSystem: ActorSystem = ActorSystem("SpetStore")

  override implicit val materializer: Materializer = ActorMaterializer()

  override implicit val ctx: ExecutionContext = actorSystem.dispatcher

  val eventBus = EventBus.ofLocal(actorSystem)

  val customerAggregate = actorSystem.actorOf(CustomerMessageBroker.props(eventBus))

  override val customerUseCase: CustomerUseCase = CustomerUseCase(customerAggregate)


  Http().bindAndHandle(
    handler = logRequestResult("log")(routes),
    interface = httpInterface,
    port = httpPort
  )
}
