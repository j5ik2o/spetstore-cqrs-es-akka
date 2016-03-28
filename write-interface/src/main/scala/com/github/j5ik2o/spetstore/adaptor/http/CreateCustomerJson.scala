package com.github.j5ik2o.spetstore.adaptor.http

import spray.json.DefaultJsonProtocol

case class CreateCustomerJson(
  name:               String,
  sexType:            Int,
  zipCode:            String,
  pref:               Int,
  cityName:           String,
  addressName:        String,
  buildingName:       Option[String],
  email:              String,
  phone:              String,
  loginName:          String,
  password:           String,
  favoriteCategoryId: Option[String]
)

object CreateCustomerJsonProtocol extends DefaultJsonProtocol {

  implicit val CreateCustomerJsonFormat = jsonFormat12(CreateCustomerJson)

}
