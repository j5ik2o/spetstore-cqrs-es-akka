package com.github.j5ik2o.spetstore.usecase

import java.util.UUID

import akka.NotUsed
import akka.actor.ActorRef
import akka.pattern.ask
import akka.stream.scaladsl.Flow
import akka.util.Timeout
import com.github.j5ik2o.spetstore.domain.basic._
import com.github.j5ik2o.spetstore.domain.customer._
import com.github.j5ik2o.spetstore.domain.item.CategoryId
import com.github.j5ik2o.spetstore.infrastructure.domainsupport.EntityProtocol.CommandRequestId

import scala.concurrent.ExecutionContext

case class CustomerUseCase(actorRef: ActorRef)(implicit timeout: Timeout, executionContext: ExecutionContext) {

//  val convertToCustomer: Flow[CreateCustomer, CreateCustomer, NotUsed] =
//    Flow[CreateCustomer].map { createCustomer =>
//      CreateCustomer(
//        CommandRequestId(),
//        CustomerId(),
//        StatusType.Enabled,
//        createCustomer.name,
//        SexType(createCustomer.sexType),
//        CustomerProfile(
//          PostalAddress(
//            ZipCode(createCustomer.zipCode),
//            createCustomer.pref,
//            createCustomer.cityName,
//            createCustomer.addressName,
//            createCustomer.buildingName
//          ),
//          Contact(
//            createCustomer.email,
//            createCustomer.phone
//          )
//        ),
//        CustomerConfig(
//          createCustomer.loginName,
//          createCustomer.password,
//          createCustomer.favoriteCategoryId.map { id =>
//            CategoryId(UUID.fromString(id))
//          }
//        ),
//        Some(1L)
//      )
//    }
//
//  val convertToUserCreated: Flow[CreateSucceeded, CustomerCreated, NotUsed] =
//    Flow[CreateSucceeded].map { createSucceeded =>
//      CustomerCreated(createSucceeded.entity)
//    }
//
//  val createCustomer: Flow[CreateCustomer, CustomerCreated, NotUsed] = Flow[CreateCustomer]
//    .via(convertToCustomer)
//    .mapAsync(1) { value =>
//      (actorRef ? value).mapTo[CreateSucceeded]
//    }.via(convertToUserCreated)

}
