# Transactional Email Service

This service sends email, with the ability to switch between two different email providers. It is written in Scala, using
the Akka HTTP Framework.

## Usage

1. Ensure that JDK 11 and sbt 1.4.x or higher are installed.
2. The service is configured to run using SendGrid. Set the `SEND_GRID_API_KEY` environment variable to the API Key
from your SendGrid account.
3. Run the service using `sbt run`.
4. To switch to MailGun, stop the server. Edit `src/main/resources/application.conf`, so that `email-client=mail-gun`.
Set the `MAIL_GUN_API_KEY` environment variable to the API Key from you MailGun account. Additionally, for MailGun, be 
sure to set the `MAIL_GUN_DOMAIN_NAME` environment variable for your account's domain name. Finally, restart the server 
with `sbt run`.

## API
This service contains one POST endpoint, which sends an email.

```
POST /email HTTP/1.1
Host: 127.0.0.1:8080
Content-Type: application/json

{
  "to": "fake@example.com",
  "to_name": "Mr. Fake",
  "from": "no-reply@fake.com",
  "from_name":"Ms. Fake",
  "subject": "A message from The Fake Family",
  "body": "<h1>Your Bill</h1><p>$10</p>"
}
```

## Testing
There is one unit test that tests the POST route. Run it like this: 

```sbt test```

## Implemenation Details
I opened the assignment on the afternoon of 08/16, and worked on it on and off over a 24-hour period. Although I have used
the Scalatra framework to build Scala services in the past, I decided to try Akka for the first time, since it is the industry 
standard, and I have been curious about it for a while. [Akka](https://doc.akka.io/docs/akka/current/typed/guide/introduction.html)
"uses the actor model, which provides a level of abstraction that makes it easier to write correct concurrent, parallel 
and distributed systems." I used [this template](https://github.com/akka/akka-http-quickstart-scala.g8) to get started.
I also found the Akka documentation to be very good and fairly easy to follow. I manually tested the service, and was able to receive email in my inbox from both providers.

## Tradeoffs
I did some basic input validation on the request body, that checks that all fields are present and all strings. I deferred
more specific validation, such as email validation, to the SendGrid and MailGun clients themselves, since they do it better.
If I had more time, I would have provided a complete set of test cases.
