package com.github.j5ik2o.spetstore.domain.customer

import java.util.UUID

import com.github.j5ik2o.spetstore.infrastructure.domainsupport.{ EntityId, Identifier }

/**
 * [[Customer]]のための識別子。
 *
 * @param value 識別子の値
 */
case class CustomerId(value: UUID = UUID.randomUUID())
  extends EntityId
