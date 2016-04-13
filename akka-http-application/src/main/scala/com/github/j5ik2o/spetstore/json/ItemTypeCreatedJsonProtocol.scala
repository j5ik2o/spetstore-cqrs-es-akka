package com.github.j5ik2o.spetstore.json

import com.github.j5ik2o.spetstore.adaptor.http.ItemTypeCreatedJson
import spray.json.DefaultJsonProtocol

object ItemTypeCreatedJsonProtocol extends DefaultJsonProtocol {

  implicit val ItemTypeCreatedJsonFormat = jsonFormat1(ItemTypeCreatedJson)

}
