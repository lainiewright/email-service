package com.example

import com.example.EmailSender.ActionPerformed
import com.example.client.{Content, EmailName, Personalization, SendGridData}

import spray.json.DefaultJsonProtocol

object JsonFormats  {
  // import the default encoders for primitive types (Int, String, Lists etc)
  import DefaultJsonProtocol._

  implicit val emailJsonFormat = jsonFormat6(Email)

  implicit val actionPerformedJsonFormat = jsonFormat1(ActionPerformed)

  implicit val emailNameFormat = jsonFormat2(EmailName)
  implicit val personalizationFormat = jsonFormat2(Personalization)
  implicit val contentFormat = jsonFormat2(Content)
  implicit val sendGridDataJsonFormat = jsonFormat4(SendGridData)
}
