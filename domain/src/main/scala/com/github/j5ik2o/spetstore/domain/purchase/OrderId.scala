package com.github.j5ik2o.spetstore.domain.purchase

import com.github.j5ik2o.spetstore.infrastructure.domainsupport.Identifier

/**
  * [[Order]]のための識別子。
  *
  * @param value 識別子の値
  */
case class OrderId(value: Long)
  extends Identifier[Long]
