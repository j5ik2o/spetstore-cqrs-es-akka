package com.github.j5ik2o.spetstore.json

import com.github.j5ik2o.spetstore.adaptor.http.CreateCustomerJson
import spray.json.DefaultJsonProtocol

object CreateCustomerJsonProtocol extends DefaultJsonProtocol {

  implicit val CreateCustomerJsonFormat = jsonFormat12(CreateCustomerJson)

}
