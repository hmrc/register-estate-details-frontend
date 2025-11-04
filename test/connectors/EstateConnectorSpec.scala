/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package connectors

import java.time.LocalDate

import base.SpecBase
import com.github.tomakehurst.wiremock.client.WireMock._
import generators.Generators
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Inside}
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import utils.WireMockHelper

import scala.concurrent.ExecutionContext.Implicits.global

class EstateConnectorSpec extends SpecBase with Generators with WireMockHelper with ScalaFutures
  with Inside with BeforeAndAfterAll with BeforeAndAfterEach with IntegrationPatience {
  implicit lazy val hc: HeaderCarrier = HeaderCarrier()

  val utr = "1000000008"
  val index = 0
  val description = "description"
  val date: LocalDate = LocalDate.parse("2019-02-03")

  "estate connector" when {

    val url: String = "/estates/correspondence/name"

    "add correspondence name" must {

      val name = "Name"

      "Return OK when the request is successful" in {

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.estates.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[EstateConnector]

        server.stubFor(
          post(urlEqualTo(url))
            .willReturn(ok)
        )

        val result = connector.addCorrespondenceName(name)

        result.futureValue.status mustBe OK

        application.stop()
      }

      "return Bad Request when the request is unsuccessful" in {

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.estates.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[EstateConnector]

        server.stubFor(
          post(urlEqualTo(url))
            .willReturn(badRequest)
        )

        val result = connector.addCorrespondenceName(name)

        result.map(response => response.status mustBe BAD_REQUEST)

        application.stop()
      }

    }

    "getting correspondence name" must {

      "Return Some(name) when it is available in the backend" in {

        val json =
          """
            |{
            |   "name": "Test Estate"
            |}
            |""".stripMargin

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.estates.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[EstateConnector]

        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(json))
        )

        val futureValue = connector.getCorrespondenceName()

        whenReady(futureValue) {
          result =>
            result mustBe Some("Test Estate")
        }

        application.stop()
      }

      "Return None when the name is not available in the backend" in {

        val json =
          """
            |{}
            |""".stripMargin

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.estates.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[EstateConnector]

        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(json))
        )

        val futureValue = connector.getCorrespondenceName()

        whenReady(futureValue) {
          result =>
            result mustBe None
        }

        application.stop()
      }
    }
  }
}
