package com.github.j5ik2o.spetstore.usecase

import com.github.j5ik2o.spetstore.domain.item.CategoryId

case class CreateItemTypeApp(
  categoryId:  CategoryId,
  name:        String,
  description: Option[String]
)

