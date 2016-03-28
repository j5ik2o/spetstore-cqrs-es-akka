package com.github.j5ik2o.spetstore.adaptor.aggregate

case class CreateFailedException(message: String) extends Exception(message)
