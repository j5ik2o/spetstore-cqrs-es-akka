package com.github.j5ik2o.spetstore.domain.item

import java.util.UUID

import com.github.j5ik2o.spetstore.infrastructure.domainsupport.EntityId

/**
 * [[Category]]のための識別子。
 *
 * @param value 識別子の値
 */
case class CategoryId(value: UUID = UUID.randomUUID())
  extends EntityId
