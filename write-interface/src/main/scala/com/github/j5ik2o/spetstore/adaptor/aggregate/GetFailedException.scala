package com.github.j5ik2o.spetstore.adaptor.aggregate

case class GetFailedException(message: String) extends Exception(message)
