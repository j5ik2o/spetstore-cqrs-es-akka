package com.github.j5ik2o.spetstore.adaptor.http

case class CreateItemTypeJson(
  categoryId:  String,
  name:        String,
  description: Option[String]
)
