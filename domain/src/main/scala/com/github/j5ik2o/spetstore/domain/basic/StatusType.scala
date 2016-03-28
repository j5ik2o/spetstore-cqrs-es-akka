package com.github.j5ik2o.spetstore.domain.basic

/**
 * [[Customer]]の状態を表す値オブジェクト。
 */
object StatusType extends Enumeration {

  /**
   * 有効
   */
  val Enabled,

  /**
   * 無効
   */
  Disabled = Value

}
