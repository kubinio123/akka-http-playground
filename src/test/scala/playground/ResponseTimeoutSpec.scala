package playground

import akka.http.scaladsl.model.StatusCodes.NetworkReadTimeout
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import akka.testkit.TestDuration
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.duration.DurationInt

class ResponseTimeoutSpec extends AnyWordSpec with Matchers with ScalatestRouteTest {

  implicit val timeout = RouteTestTimeout(20.seconds.dilated)

  "Http server" should {

    "respect idle-timeout" in {
      Get("/hello") ~> HttpServer.route ~> check {
        response.status shouldBe NetworkReadTimeout
      }
    }
  }

}
