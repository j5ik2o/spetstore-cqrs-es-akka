package com.github.j5ik2o.spetstore.domain.customer

import com.github.j5ik2o.spetstore.domain.item.CategoryId

/**
  * [[Customer]]の設定を表す値オブジェクト。
  *
  * @param loginName          ログイン名
  * @param password           パスワード
  * @param favoriteCategoryId お気に入りカテゴリID
  */
case class CustomerConfig
(loginName: String,
 password: String,
 favoriteCategoryId: Option[CategoryId])

