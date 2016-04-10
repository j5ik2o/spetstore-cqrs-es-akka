package com.github.j5ik2o.spetstore.adaptor.http

import akka.NotUsed
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.stream.Materializer
import akka.stream.scaladsl.{ Flow, Keep, Sink, Source }
import com.github.j5ik2o.spetstore.domain.basic.Pref
import com.github.j5ik2o.spetstore.usecase.{ CreateCustomer, CustomerCreated, CustomerUseCase }

import scala.concurrent.{ ExecutionContext, Future }

trait Route extends SprayJsonSupport {

  import CreateCustomerJsonProtocol._
  import CustomerCreatedJsonProtocol._

  implicit val materializer: Materializer

  implicit val ctx: ExecutionContext

  val customerUseCase: CustomerUseCase

  private val convertToCustomerCreated: Flow[CustomerCreated, CustomerCreatedJson, NotUsed] =
    Flow[CustomerCreated].map { e =>
      CustomerCreatedJson(e.entity.id.value.toString)
    }

  private val convertToCreateCustomer: Flow[CreateCustomerJson, CreateCustomer, NotUsed] =
    Flow[CreateCustomerJson].map { createCustomerJson =>
      CreateCustomer(
        createCustomerJson.name,
        createCustomerJson.sexType,
        createCustomerJson.zipCode,
        Pref(createCustomerJson.pref),
        createCustomerJson.cityName,
        createCustomerJson.addressName,
        createCustomerJson.buildingName,
        createCustomerJson.email,
        createCustomerJson.phone,
        createCustomerJson.loginName,
        createCustomerJson.password,
        createCustomerJson.favoriteCategoryId
      )
    }

  //  val customerWriteRoutes = path("customers") {
  //    post {
  //      entity(as[CreateCustomerJson]) { createCustomerJson =>
  //        val result: Future[CustomerCreatedJson] =
  //          Source.single(createCustomerJson)
  //            .via(convertToCreateCustomer)
  //            .via(customerUseCase.createCustomer)
  //            .via(convertToCustomerCreated)
  //            .toMat(Sink.head)(Keep.right)
  //            .run()
  //        onSuccess(result) { customerCreatedJson =>
  //          complete(customerCreatedJson)
  //        }
  //      }
  //    }
  //  }

  val routes = ??? // customerWriteRoutes

}
