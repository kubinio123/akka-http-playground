package playground

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.event.Logging.LogLevel
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, _}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.RouteResult
import akka.http.scaladsl.server.RouteResult.{Complete, Rejected}
import akka.http.scaladsl.server.directives.{DebuggingDirectives, LogEntry, LoggingMagnet}

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

object HttpServerRoutingMinimal {

  def main(args: Array[String]): Unit = {

    def akkaResponseTimeLoggingFunction(loggingAdapter: LoggingAdapter, requestTimestamp: Long, level: LogLevel = Logging.InfoLevel)(
        req: HttpRequest
    )(res: RouteResult): Unit = {
      val entry = res match {
        case Complete(resp) =>
          println("COMPLETED RESPONSE " + resp)
          val responseTimestamp: Long = System.nanoTime
          val elapsedTime: Long = (responseTimestamp - requestTimestamp) / 1000000
          val loggingString = s"""Logged Request:${req.method}:${req.uri}:${resp.status}:$elapsedTime"""
          LogEntry(loggingString, level)
        case Rejected(reason) =>
          LogEntry(s"Rejected Reason: ${reason.mkString(",")}", level)
      }
      entry.logTo(loggingAdapter)
    }
    def printResponseTime(log: LoggingAdapter) = {
      val requestTimestamp = System.nanoTime
      akkaResponseTimeLoggingFunction(log, requestTimestamp) _
    }

    val logResponseTime = DebuggingDirectives.logRequestResult(LoggingMagnet(printResponseTime))

    implicit val system = ActorSystem(Behaviors.empty, "my-system")
    implicit val executionContext = system.executionContext

    val idleTimeout = system.settings.config.getDuration("akka.http.server.idle-timeout").toMillis.toInt.millis

    class IdleTimeoutExceededException extends RuntimeException

    val route = {
      path("hello") {
        get {
          complete {
            Future.firstCompletedOf(
              Seq(
                akka.pattern.after(idleTimeout)(Future.failed(new IdleTimeoutExceededException)),
                Future {
                  Thread.sleep(10000)
                  HttpResponse(StatusCodes.OK)
                }
              )
            )
          }
        }
      }
    }

    Http().newServerAt("localhost", 8080).bind(route)
  }
}
