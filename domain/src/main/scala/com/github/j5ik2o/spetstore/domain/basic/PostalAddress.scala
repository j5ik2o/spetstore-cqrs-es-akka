package com.github.j5ik2o.spetstore.domain.basic

/**
 * 住所を表す値オブジェクト。
 *
 * @param zipCode [[ZipCode]]
 * @param pref [[Pref]]
 * @param cityName 市区町村名
 * @param addressName 地番名
 * @param buildingName 建物名
 */
case class PostalAddress
(zipCode: ZipCode,
 pref: Pref.Value,
 cityName: String,
 addressName: String,
 buildingName: Option[String] = None)

