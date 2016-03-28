package com.github.j5ik2o.spetstore.domain.purchase

import com.github.j5ik2o.spetstore.infrastructure.domainsupport.Identifier

/**
  * [[com.github.j5ik2o.spetstore.domain.model.purchase.Cart]]のための識別子。
  *
  * @param value 識別子の値
  */
case class CartId(value: Long)
  extends Identifier[Long]

