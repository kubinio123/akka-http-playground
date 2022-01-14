package playground

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.StatusCodes.NetworkReadTimeout
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.RouteResult.Complete
import akka.http.scaladsl.server.{Directive0, ExceptionHandler, RouteResult}
import akka.stream.scaladsl.Flow
import akka.util.ByteString

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

object AroundDirectives {

  object IdleTimeoutException extends RuntimeException

  val timeoutResponse: HttpResponse = HttpResponse(NetworkReadTimeout, entity = "Unable to serve response within time limit.")

  def aroundRequest(
      onRequest: HttpRequest => Try[RouteResult] => Unit
  )(implicit system: ActorSystem[Nothing], ec: ExecutionContext): Directive0 = {

    val idleTimeout = system.settings.config.getDuration("akka.http.server.idle-timeout").toMillis

    lazy val idleTimeoutFuture = akka.pattern.after(idleTimeout.toInt.millis)(Future.failed[RouteResult](IdleTimeoutException))

    extractRequestContext.flatMap { ctx =>
      {
        val onDone = onRequest(ctx.request)
        mapInnerRoute { inner =>
          withRequestTimeoutResponse(_ => {
            onDone(Success(Complete(timeoutResponse)))
            timeoutResponse
          }) {
            handleExceptions { ExceptionHandler { case IdleTimeoutException => complete(timeoutResponse) } } {
              inner.andThen { resultFuture =>
                Future
                  .firstCompletedOf(Seq(resultFuture, idleTimeoutFuture))
                  .map {
                    case c @ Complete(response) =>
                      Complete(response.mapEntity { entity =>
                        if (entity.isKnownEmpty()) {
                          onDone(Success(c))
                          entity
                        } else {
                          // On an empty entity, `transformDataBytes` unsets `isKnownEmpty`.
                          // Call onDone right away, since there's no significant amount of
                          // data to send, anyway.
                          entity.transformDataBytes(Flow[ByteString].watchTermination() { case (m, f) =>
                            f.map(_ => c).onComplete(onDone)
                            m
                          })
                        }
                      })
                    case other =>
                      onDone(Success(other))
                      other
                  }
                  .andThen { case Failure(ex) => onDone(Failure(ex)) }
              }
            }
          }
        }
      }
    }
  }
}
