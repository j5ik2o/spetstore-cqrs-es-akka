package com.github.j5ik2o.spetstore.domain.purchase

import com.github.j5ik2o.spetstore.domain.basic.StatusType
import com.github.j5ik2o.spetstore.domain.item.ItemId

/**
 * 注文する商品を表す値オブジェクト。
 *
 * @param itemId   [[com.github.j5ik2o.spetstore.domain.item.Item]]のI
 * @param quantity 数量
 */
case class OrderItem(
  id:       OrderItemId,
  status:   StatusType.Value,
  no:       Int,
  itemId:   ItemId,
  quantity: Int,
  version:  Option[Long]
) {

}

/**
 * コンパニオンオブジェクト。
 */
object OrderItem {

  /**
   * [[CartItem]]から
   * [[OrderItem]]を
   * 生成する。
   *
   * @param cartItem [[CartItem]]
   * @return [[OrderItem]]
   */
  def fromCartItem(orderItemId: OrderItemId, cartItem: CartItem): OrderItem =
    OrderItem(orderItemId, cartItem.status, cartItem.no, cartItem.itemId, cartItem.quantity, None)

}
