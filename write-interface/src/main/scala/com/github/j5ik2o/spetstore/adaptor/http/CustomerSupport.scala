package com.github.j5ik2o.spetstore.adaptor.http

import akka.NotUsed
import akka.stream.scaladsl.{ Flow, Keep, RunnableGraph, Sink, Source }
import com.github.j5ik2o.spetstore.domain.basic.Pref
import com.github.j5ik2o.spetstore.usecase.{ CreateCustomerApp, CustomerCreatedApp, CustomerUseCase }

import scala.concurrent.Future

trait CustomerSupport {

  val customerUseCase: CustomerUseCase

  protected val convertToCreateCustomer: Flow[CreateCustomerJson, CreateCustomerApp, NotUsed] =
    Flow[CreateCustomerJson].map { createCustomerJson =>
      CreateCustomerApp(
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

  protected val convertToCustomerCreated: Flow[CustomerCreatedApp, CustomerCreatedJson, NotUsed] =
    Flow[CustomerCreatedApp].map { e =>
      CustomerCreatedJson(e.entityId.value.toString)
    }

  def createCustomerGraph(createCustomerJson: CreateCustomerJson): RunnableGraph[Future[CustomerCreatedJson]] =
    Source.single(createCustomerJson)
      .via(convertToCreateCustomer)
      .via(customerUseCase.createCustomer)
      .via(convertToCustomerCreated)
      .toMat(Sink.head)(Keep.right)
}
