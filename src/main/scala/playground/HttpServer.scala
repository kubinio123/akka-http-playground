package playground

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.RouteResult.Rejected
import akka.http.scaladsl.server.{Route, RouteResult}
import playground.AroundDirectives.{IdleTimeoutException, aroundRequest}

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success, Try}

object HttpServer {

  implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "my-system")
  implicit val ec: ExecutionContextExecutor = system.executionContext

  def timeRequest(request: HttpRequest): Try[RouteResult] => Unit = {
    val start = System.currentTimeMillis()

    {
      case Success(_) =>
        val d = System.currentTimeMillis() - start
        system.log.info(s"${request.uri} took: $d ms")

      case Success(Rejected(_)) => ()

      case Failure(IdleTimeoutException) =>
        val d = System.currentTimeMillis() - start
        system.log.info(s"${request.uri} took: $d ms")

      case Failure(_) => ()
    }
  }

  val route: Route = {
    path("hello") {
      aroundRequest(timeRequest)(system, ec) {
        get {
          complete {
            Future {
              Thread.sleep(10000)
              HttpResponse(StatusCodes.OK)
            }
          }
        }
      }
    }
  }

  def main(args: Array[String]): Unit = {
    Http().newServerAt("localhost", 8080).bind(route)
  }
}
