package com.github.j5ik2o.spetstore.domain.purchase

import java.util.UUID

import com.github.j5ik2o.spetstore.infrastructure.domainsupport.EntityId

/**
 * [[com.github.j5ik2o.spetstore.domain.model.purchase.Cart]]のための識別子。
 *
 * @param value 識別子の値
 */
case class CartId(value: UUID = UUID.randomUUID())
  extends EntityId

