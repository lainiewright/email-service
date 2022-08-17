package com.example.client

import akka.http.scaladsl.model.HttpResponse
import com.example.Email
import org.jsoup.Jsoup

import scala.concurrent.Future

abstract class BaseClient {
  /*
   * Abstract method for sending an email through an email client.
   */
  def sendEmail(email: Email): Future[HttpResponse]

  // removes html tags
  protected def parseHtml(html: String): String = {
    Jsoup.parse(html).wholeText()
  }
}
