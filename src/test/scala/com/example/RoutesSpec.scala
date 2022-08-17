package com.example

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.example.EmailSender.{ActionPerformed, Command, SendEmail}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class RoutesSpec extends AnyWordSpec with Matchers with ScalaFutures with ScalatestRouteTest {

  lazy val testKit = ActorTestKit()
  implicit def typedSystem = testKit.system
  override def createActorSystem(): akka.actor.ActorSystem =
    testKit.system.classicSystem

  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import JsonFormats._

  "Routes" should {
    "be able to send an email (POST /email)" in {
      val email = Email(
        to = "fake@example.com",
        to_name = "Mr. Fake",
        from = "no-reply@fake.com",
        from_name = "Ms. Fake",
        subject = "A message from The Fake Family",
        body = "<h1>Your Bill</h1><p>$10</p>"
      )
      val emailEntity = Marshal(email).to[MessageEntity].futureValue

      val mockedBehavior = Behaviors.receiveMessage[SendEmail] { msg =>
        msg.replyTo ! ActionPerformed("Email to fake@example.com sent via SendGrid.")
        Behaviors.same
      }
      val probe = testKit.createTestProbe[SendEmail]()
      val mockedEmailSender = testKit.spawn(Behaviors.monitor(probe.ref, mockedBehavior)).asInstanceOf[ActorRef[Command]]
      lazy val routes = new Routes(mockedEmailSender).routes

      val request = Post("/email").withEntity(emailEntity)
      request ~> routes ~> check {
        status should ===(StatusCodes.Created)
        contentType should ===(ContentTypes.`application/json`)
        entityAs[String] should ===("{\"description\":\"Email to fake@example.com sent via SendGrid.\"}")
      }
    }
  }
}
