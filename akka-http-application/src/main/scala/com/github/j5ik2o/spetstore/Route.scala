package com.github.j5ik2o.spetstore

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.stream.Materializer
import com.github.j5ik2o.spetstore.adaptor.http.{CreateCustomerJson, CustomerSupport}
import com.github.j5ik2o.spetstore.json.{CreateCustomerJsonProtocol, CustomerCreatedJsonProtocol}

import scala.concurrent.ExecutionContext

trait Route extends SprayJsonSupport with CustomerSupport {

  import CreateCustomerJsonProtocol._
  import CustomerCreatedJsonProtocol._

  implicit val materializer: Materializer

  implicit val executor: ExecutionContext

  val customerWriteRoutes = path("customers") {
    post {
      entity(as[CreateCustomerJson]) { createCustomerJson =>
        val result = createCustomerGraph(createCustomerJson).run()
        onSuccess(result) { customerCreatedJson =>
          complete(customerCreatedJson)
        }
      }
    }
  }

  val routes = customerWriteRoutes

}
