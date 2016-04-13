package com.github.j5ik2o.spetstore

import akka.http.scaladsl.server.Directives._
import com.github.j5ik2o.spetstore.adaptor.http.{ CreateItemTypeJson, ItemTypeSupport }
import com.github.j5ik2o.spetstore.json.{ CreateItemTypeJsonProtocol, ItemTypeCreatedJsonProtocol }

trait ItemTypeRoute extends ItemTypeSupport {
  this: Route =>

  import CreateItemTypeJsonProtocol._
  import ItemTypeCreatedJsonProtocol._

  val itemTypeWriteRoutes = path("item-types") {
    post {
      entity(as[CreateItemTypeJson]) { createItemTypeJson =>
        val result = createItemTypeGraph(createItemTypeJson).run()
        onSuccess(result) { itemTypeCreatedJson =>
          complete(itemTypeCreatedJson)
        }
      }
    }
  }
}
