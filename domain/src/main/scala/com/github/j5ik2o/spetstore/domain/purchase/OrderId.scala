package com.github.j5ik2o.spetstore.domain.purchase

import java.util.UUID

import com.github.j5ik2o.spetstore.infrastructure.domainsupport.EntityId

/**
 * [[Order]]のための識別子。
 *
 * @param value 識別子の値
 */
case class OrderId(value: UUID)
  extends EntityId
