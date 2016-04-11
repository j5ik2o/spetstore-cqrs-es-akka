package com.github.j5ik2o.spetstore.domain.purchase

import java.util.UUID

import com.github.j5ik2o.spetstore.infrastructure.domainsupport.EntityId

case class OrderItemId(value: UUID = UUID.randomUUID())
  extends EntityId
