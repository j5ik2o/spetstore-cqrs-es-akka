package com.github.j5ik2o.spetstore.domain.item

import java.util.UUID

import com.github.j5ik2o.spetstore.infrastructure.domainsupport.EntityId

case class InventoryId(value: UUID)
  extends EntityId
