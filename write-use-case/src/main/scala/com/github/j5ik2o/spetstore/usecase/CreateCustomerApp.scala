package com.github.j5ik2o.spetstore.usecase

import com.github.j5ik2o.spetstore.domain.basic.Pref
import com.github.j5ik2o.spetstore.domain.item.CategoryId

case class CreateCustomerApp(
  name:         String,
  sexType:      Int,
  zipCode:      String,
  pref:         Pref.Value,
  cityName:     String,
  addressName:  String,
  buildingName: Option[String],
  email:        String, phone: String,
  loginName:          String,
  password:           String,
  favoriteCategoryId: Option[CategoryId]
)
