package com.github.j5ik2o.spetstore

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.stream.Materializer

import scala.concurrent.ExecutionContext

trait Route extends SprayJsonSupport with CustomerRoute with ItemTypeRoute {

  implicit val materializer: Materializer

  implicit val executor: ExecutionContext

  val routes = customerWriteRoutes ~ itemTypeWriteRoutes

}
