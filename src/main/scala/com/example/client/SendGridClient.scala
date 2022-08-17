package com.example.client

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.javadsl.model.headers._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import com.example.{Email, JsonFormats}
import spray.json._

import scala.concurrent.Future

case class EmailName(email: String, name: String)
case class Personalization(to: List[EmailName], subject: String)
case class Content(`type`: String, value: String)
case class SendGridData(personalizations: List[Personalization],
                        content: List[Content],
                        from: EmailName,
                        reply_to: EmailName)

class SendGridClient extends BaseClient {
  import JsonFormats._

  val apiKey = sys.env.getOrElse("SEND_GRID_API_KEY", "")
  val uri = "https://api.sendgrid.com/v3/mail/send"
  val headers = Seq(Authorization.oauth2(apiKey), ContentTypes.`application/json`)

  private def buildRequestData(email: Email): SendGridData = {
    val to = EmailName(email.to, email.to_name)
    val from = EmailName(email.from, email.from_name)
    val content = Content("text/plain", parseHtml(email.body))
    val personalization = Personalization(List(to), email.subject)
    SendGridData(
      personalizations = List(personalization),
      content = List(content),
      from = from,
      reply_to = from)
  }

  def sendEmail(email: Email): Future[HttpResponse] = {
    val data = buildRequestData(email)
    val httpRequest = HttpRequest(
      method = HttpMethods.POST,
      uri = "https://api.sendgrid.com/v3/mail/send",
      headers = Seq(Authorization.oauth2(apiKey)),
      entity = HttpEntity(ContentTypes.`application/json`, data.toJson.toString)
    )

    implicit val system = ActorSystem(Behaviors.empty, "SingleRequest")
    implicit val executionContext = system.executionContext
    Http().singleRequest(httpRequest)
  }
}
