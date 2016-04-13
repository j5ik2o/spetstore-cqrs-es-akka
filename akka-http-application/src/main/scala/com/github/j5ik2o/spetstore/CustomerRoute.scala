package com.github.j5ik2o.spetstore

import akka.http.scaladsl.server.Directives._
import com.github.j5ik2o.spetstore.adaptor.http.{ CreateCustomerJson, CustomerSupport }
import com.github.j5ik2o.spetstore.json.{ CreateCustomerJsonProtocol, CustomerCreatedJsonProtocol }

trait CustomerRoute extends CustomerSupport {
  this: Route =>

  import CreateCustomerJsonProtocol._
  import CustomerCreatedJsonProtocol._

  val customerWriteRoutes = path("customers") {
    post {
      entity(as[CreateCustomerJson]) { createCustomerJson =>
        val result = createCustomerGraph(createCustomerJson).run()
        onSuccess(result) { customerCreatedJson =>
          complete(customerCreatedJson)
        }
      }
    }
  }

}
