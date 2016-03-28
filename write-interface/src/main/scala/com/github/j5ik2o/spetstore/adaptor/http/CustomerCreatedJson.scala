package com.github.j5ik2o.spetstore.adaptor.http

import spray.json.DefaultJsonProtocol

case class CustomerCreatedJson(id: String)

object CustomerCreatedJsonProtocol extends DefaultJsonProtocol {

  implicit val CustomerCreatedJsonFormat = jsonFormat1(CustomerCreatedJson)

}
