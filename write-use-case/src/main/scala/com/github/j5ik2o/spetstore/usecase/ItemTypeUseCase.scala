package com.github.j5ik2o.spetstore.usecase

import akka.NotUsed
import akka.actor.ActorRef
import akka.pattern.ask
import akka.stream.scaladsl.Flow
import akka.util.Timeout
import com.github.j5ik2o.spetstore.domain.item.ItemTypeAggregateProtocol.Create.{ CreateItemType, CreateSucceeded }
import com.github.j5ik2o.spetstore.domain.item.ItemTypeId
import com.github.j5ik2o.spetstore.infrastructure.domainsupport.EntityProtocol.CommandRequestId

import scala.concurrent.ExecutionContext

case class ItemTypeUseCase(actorRef: ActorRef)(implicit timeout: Timeout, executionContext: ExecutionContext) {

  private val convertToCreateItemType: Flow[CreateItemTypeApp, CreateItemType, NotUsed] =
    Flow[CreateItemTypeApp].map { createItemTypeApp =>
      CreateItemType(
        CommandRequestId(),
        ItemTypeId(),
        createItemTypeApp.categoryId,
        createItemTypeApp.name,
        createItemTypeApp.description
      )
    }

  private val convertToItemTypeCreated: Flow[CreateSucceeded, ItemTypeCreatedApp, NotUsed] =
    Flow[CreateSucceeded].map { createSucceeded =>
      ItemTypeCreatedApp(createSucceeded.entityId)
    }

  val createItemType: Flow[CreateItemTypeApp, ItemTypeCreatedApp, NotUsed] = Flow[CreateItemTypeApp]
    .via(convertToCreateItemType)
    .mapAsync(1) { value =>
      (actorRef ? value).mapTo[CreateSucceeded]
    }.via(convertToItemTypeCreated)

}
