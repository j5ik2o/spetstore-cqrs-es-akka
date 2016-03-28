package com.github.j5ik2o.spetstore.domain.purchase

import com.github.j5ik2o.spetstore.infrastructure.domainsupport.Identifier

case class OrderItemId(value: Long)
  extends Identifier[Long]
