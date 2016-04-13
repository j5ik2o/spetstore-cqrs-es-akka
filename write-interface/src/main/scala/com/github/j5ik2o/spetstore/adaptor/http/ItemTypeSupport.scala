package com.github.j5ik2o.spetstore.adaptor.http

import java.util.UUID

import akka.NotUsed
import akka.stream.scaladsl.{ Flow, Keep, RunnableGraph, Sink, Source }
import com.github.j5ik2o.spetstore.domain.item.CategoryId
import com.github.j5ik2o.spetstore.usecase.{ CreateItemTypeApp, CustomerCreatedApp, ItemTypeCreatedApp, ItemTypeUseCase }

import scala.concurrent.Future

trait ItemTypeSupport {

  val itemTypeUseCase: ItemTypeUseCase

  protected val convertToCreateItemType: Flow[CreateItemTypeJson, CreateItemTypeApp, NotUsed] =
    Flow[CreateItemTypeJson].map { json =>
      CreateItemTypeApp(
        CategoryId(UUID.fromString(json.categoryId)),
        json.name,
        json.description
      )
    }

  protected val convertToItemTypeCreated: Flow[ItemTypeCreatedApp, ItemTypeCreatedJson, NotUsed] =
    Flow[ItemTypeCreatedApp].map { e =>
      ItemTypeCreatedJson(e.entityId.value.toString)
    }

  def createItemTypeGraph(json: CreateItemTypeJson): RunnableGraph[Future[ItemTypeCreatedJson]] =
    Source.single(json)
      .via(convertToCreateItemType)
      .via(itemTypeUseCase.createItemType)
      .via(convertToItemTypeCreated)
      .toMat(Sink.head)(Keep.right)
}
