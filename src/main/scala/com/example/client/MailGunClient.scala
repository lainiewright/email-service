package com.example.client

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.javadsl.model.headers.Authorization
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, HttpResponse}
import com.example.Email

import scala.concurrent.Future

class MailGunClient extends BaseClient {
  def sendEmail(email: Email): Future[HttpResponse] = {
    val apiKey = sys.env.getOrElse("MAIL_GUN_API_KEY", "")
    val domainName = sys.env.getOrElse("MAIL_GUN_DOMAIN_NAME", "")

    val httpRequest = HttpRequest(
      method = HttpMethods.POST,
      uri = s"https://api.mailgun.net/v3/$domainName/messages",
      headers = Seq(Authorization.basic("api", apiKey)),
      entity = HttpEntity(ContentTypes.`application/x-www-form-urlencoded`,
        s"from=${email.from}&to=${email.to}&subject=${email.subject}&text=${parseHtml(email.body)}")
    )

    implicit val system = ActorSystem(Behaviors.empty, "SingleRequest")
    implicit val executionContext = system.executionContext
    Http().singleRequest(httpRequest)
  }
}
