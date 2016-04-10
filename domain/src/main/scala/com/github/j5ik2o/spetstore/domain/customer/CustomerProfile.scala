package com.github.j5ik2o.spetstore.domain.customer

import com.github.j5ik2o.spetstore.domain.basic.{Contact, PostalAddress, SexType}

/**
  * [[Customer]]のプロフィールを表す値オブジェクト。
  *
  * @param postalAddress [[PostalAddress]]
  * @param contact       [[Contact]]
  */
case class CustomerProfile(
                            name: String,
                            sexType: SexType.Value,
                            postalAddress: PostalAddress,
                            contact: Contact
                          )

