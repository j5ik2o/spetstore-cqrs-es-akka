package com.github.j5ik2o.spetstore.usecase

import akka.NotUsed
import akka.actor.ActorRef
import akka.pattern.ask
import akka.stream.scaladsl.Flow
import akka.util.Timeout
import com.github.j5ik2o.spetstore.domain.basic._
import com.github.j5ik2o.spetstore.domain.customer.CustomerAggregateProtocol.Create.{ CreateCustomer, CreateSucceeded }
import com.github.j5ik2o.spetstore.domain.customer._
import com.github.j5ik2o.spetstore.infrastructure.domainsupport.EntityProtocol.CommandRequestId

import scala.concurrent.ExecutionContext

case class CustomerUseCase(actorRef: ActorRef)(implicit timeout: Timeout, executionContext: ExecutionContext) {

  private val convertToCustomer: Flow[CreateCustomerApp, CreateCustomer, NotUsed] =
    Flow[CreateCustomerApp].map { createCustomerApp =>
      CreateCustomer(
        CommandRequestId(),
        CustomerId(),
        StatusType.Enabled,
        CustomerProfile(
          createCustomerApp.name,
          SexType(createCustomerApp.sexType),
          PostalAddress(
            ZipCode(createCustomerApp.zipCode),
            createCustomerApp.pref,
            createCustomerApp.cityName,
            createCustomerApp.addressName,
            createCustomerApp.buildingName
          ),
          Contact(
            createCustomerApp.email,
            createCustomerApp.phone
          )
        ),
        CustomerConfig(
          createCustomerApp.loginName,
          createCustomerApp.password,
          createCustomerApp.favoriteCategoryId
        ),
        Some(1L)
      )
    }

  private val convertToCustomerCreated: Flow[CreateSucceeded, CustomerCreatedApp, NotUsed] =
    Flow[CreateSucceeded].map { createSucceeded =>
      CustomerCreatedApp(createSucceeded.entityId)
    }

  val createCustomer: Flow[CreateCustomerApp, CustomerCreatedApp, NotUsed] = Flow[CreateCustomerApp]
    .via(convertToCustomer)
    .mapAsync(1) { value =>
      (actorRef ? value).mapTo[CreateSucceeded]
    }.via(convertToCustomerCreated)

}
