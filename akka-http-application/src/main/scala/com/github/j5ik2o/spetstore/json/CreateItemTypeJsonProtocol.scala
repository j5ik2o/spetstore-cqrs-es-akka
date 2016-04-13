package com.github.j5ik2o.spetstore.json

import com.github.j5ik2o.spetstore.adaptor.http.CreateItemTypeJson
import spray.json.DefaultJsonProtocol

object CreateItemTypeJsonProtocol extends DefaultJsonProtocol {

  implicit val ItemTypeJsonFormat = jsonFormat3(CreateItemTypeJson)

}
