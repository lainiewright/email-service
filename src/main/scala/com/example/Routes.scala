package com.example

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import scala.concurrent.Future
import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern._
import akka.util.Timeout
import com.example.EmailSender._

class Routes(emailSender: ActorRef[Command])(implicit val system: ActorSystem[_]) {

  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import JsonFormats._

  // If ask takes more time than this to complete the request is failed
  private implicit val timeout = Timeout.create(system.settings.config.getDuration("my-app.routes.ask-timeout"))

  def sendEmail(email: Email): Future[ActionPerformed] =
    emailSender.ask(SendEmail(email, _))

  val routes: Route =
    // POST /email endpoint
    pathPrefix("email") {
      post {
        // handles requests that do not contain all field or contain non-string fields
        entity(as[Email]) { email =>
          onSuccess(sendEmail(email)) { performed =>
            complete(StatusCodes.Created, performed)
          }
        }
      }
    }
}
