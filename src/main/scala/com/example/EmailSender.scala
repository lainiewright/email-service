package com.example

import akka.actor.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import com.example.client.{BaseClient, MailGunClient, SendGridClient}
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContextExecutor

final case class Email(to: String,
                         to_name: String,
                         from: String,
                         from_name: String,
                         subject: String,
                         body: String)

object EmailSender {
  // actor protocol
  sealed trait Command
  final case class SendEmail(email: Email, replyTo: ActorRef[ActionPerformed]) extends Command
  final case class ActionPerformed(description: String)

  def apply(): Behavior[Command] =
    Behaviors.receiveMessage {
      case SendEmail(email, replyTo) =>
        sendEmail(email, replyTo)
        Behaviors.same
    }

  /* Create either a SendGrid or MailGun client, depending on the app configuration.
   *
   * throws IllegalArgumentException
   */
  private def instantiateClient(): BaseClient = {
    val clientToUse = ConfigFactory.load().getString("my-app.email-client")
    if (clientToUse.equals("send-grid")) {
      new SendGridClient
    } else if (clientToUse.equals("mail-gun")) {
      new MailGunClient
    } else {
      throw new IllegalArgumentException(s"Email client '$clientToUse' is not a valid email client.")
    }
  }

  /* Sends an email and notifies the actor
   *
   * @param email - the Email object
   * @param replyTo - a reference to the actor that will receive the message of the action performed
   */
  private def sendEmail(email: Email, replyTo: ActorRef[ActionPerformed]): Unit = {
    val client = instantiateClient()

    implicit val system: ActorSystem = ActorSystem()
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    // leverage the fact that each client returns a different status code, in order to output a custom response
    client.sendEmail(email).map {
      case response @ HttpResponse(StatusCodes.OK, _, _, _) => replyTo ! ActionPerformed(s"Email to ${email.to} sent via MailGun.")
      case response @ HttpResponse(StatusCodes.Accepted, _, _, _) => replyTo ! ActionPerformed(s"Email to ${email.to} sent via SendGrid.")
      case _ => sys.error("email client failure")
    }
  }
}
