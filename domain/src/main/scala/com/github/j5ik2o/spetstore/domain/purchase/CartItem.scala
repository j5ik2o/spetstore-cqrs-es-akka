package com.github.j5ik2o.spetstore.domain.purchase

import com.github.j5ik2o.spetstore.domain.basic.StatusType
import com.github.j5ik2o.spetstore.domain.item.ItemId

import scala.util.Try

/**
 * 注文する商品を表す値オブジェクト。
 *
 * @param itemId   [[Item]]のID
 * @param quantity 数量
 * @param inStock  後で購入する場合true
 */
case class CartItem(
    id:       CartItemId,
    status:   StatusType.Value,
    no:       Int,
    itemId:   ItemId,
    quantity: Int,
    inStock:  Boolean,
    version:  Option[Long]
) {

  /**
   * 数量をインクリメントする。
   *
   * @return [[CartItem]]
   */
  def incrementQuantity: CartItem = addQuantity(1)

  /**
   * 数量を追加する。
   *
   * @return [[CartItem]]
   */
  def addQuantity(otherQuantity: Int): CartItem = copy(quantity = quantity + otherQuantity)

  /**
   * 数量を更新する。
   *
   * @param quantity 数量
   * @return [[CartItem]]
   */
  def withQuantity(quantity: Int): CartItem = copy(quantity = quantity)

}
