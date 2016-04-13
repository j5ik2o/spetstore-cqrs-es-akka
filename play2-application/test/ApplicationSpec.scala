
import com.github.tototoshi.play2.json4s.jackson.Json4s
import com.github.tototoshi.play2.json4s.test.jackson.Helpers._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.scalatestplus.play._
import play.api.Configuration
import play.api.test.Helpers._
import play.api.test._

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
class ApplicationSpec extends PlaySpec with OneAppPerTest {

  val configuration = Configuration.empty

  val json4s = new Json4s(configuration)

  import json4s._

  "CustomerController" should {

    "create" in {
      val create = route(app, FakeRequest(POST, "/customers").withJson4sBody(parse(
        """
          |{
          |  "name" : "hoge",
          |  "sexType" : 1,
          |  "zipCode" : "111-1111",
          |  "pref" : 1,
          |  "cityName" : "test",
          |  "addressName" : "test",
          |  "buildingName" : null,
          |  "email" : "test@test.com",
          |  "phone" : "090-0000-0000",
          |  "loginName" : "test",
          |  "password" : "test",
          |  "favoriteCategoryId": null
          |}
        """.stripMargin
      ))).get

      status(create) mustBe OK
      contentType(create) mustBe Some("application/json")
    }

  }

  "ItemTypeController" should {

    "create" in {
      val create = route(app, FakeRequest(POST, "/item-types").withJson4sBody(parse(
        """
          |{
          |  "categoryId" : "7fdd6574-2f98-4315-a9b7-4bbe048eb29d",
          |  "name" : "test",
          |  "description": null
          |}
        """.stripMargin
      ))).get

      status(create) mustBe OK
      contentType(create) mustBe Some("application/json")
    }

  }

}
