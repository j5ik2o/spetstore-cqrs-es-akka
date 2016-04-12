package com.github.j5ik2o.spetstore

import com.github.j5ik2o.spetstore.adaptor.http.CustomerCreatedJson
import spray.json.DefaultJsonProtocol

object CustomerCreatedJsonProtocol extends DefaultJsonProtocol {

  implicit val CustomerCreatedJsonFormat = jsonFormat1(CustomerCreatedJson)

}
