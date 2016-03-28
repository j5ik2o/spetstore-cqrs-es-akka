package com.github.j5ik2o.spetstore.domain.item

import com.github.j5ik2o.spetstore.infrastructure.domainsupport.Identifier

/**
  * [[Item]]のための識別子
  *
  * @param value 識別子の値
  */
case class ItemId(value: Long)
  extends Identifier[Long]
