/*
 * Copyright 2022 HM Revenue & Customs
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

import base.SpecBase
import com.github.tomakehurst.wiremock.client.WireMock.{okJson, urlEqualTo, _}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import utils.WireMockHelper

import scala.concurrent.ExecutionContext.Implicits.global

class EstatesStoreConnectorSpec extends SpecBase
  with ScalaFutures
  with IntegrationPatience
  with WireMockHelper {

  implicit lazy val hc: HeaderCarrier = HeaderCarrier()

  "trusts store connector" must {

    "return OK with the current task status" in {
      val application = applicationBuilder()
        .configure(
          Seq(
            "microservice.services.estates-store.port" -> server.port(),
            "auditing.enabled" -> false
          ): _*
        ).build()

      val connector = application.injector.instanceOf[EstatesStoreConnector]

      val json = Json.parse(
        """
          |{
          |  "details": true,
          |  "personalRepresentative": false,
          |  "deceased": false,
          |  "yearsOfTaxLiability": false
          |}
          |""".stripMargin)

      server.stubFor(
        post(urlEqualTo("/estates-store/register/tasks/estate-details"))
          .willReturn(okJson(json.toString))
      )

      val futureResult = connector.setTaskComplete()

      whenReady(futureResult) {
        r =>
          r.status mustBe 200
      }

      application.stop()
    }

    "return default tasks when a failure occurs" in {
      val application = applicationBuilder()
        .configure(
          Seq(
            "microservice.services.estates-store.port" -> server.port(),
            "auditing.enabled" -> false
          ): _*
        ).build()

      val connector = application.injector.instanceOf[EstatesStoreConnector]

      server.stubFor(
        post(urlEqualTo("/estates-store/register/tasks/estate-details"))
          .willReturn(serverError())
      )

      val futureResult = connector.setTaskComplete()

      whenReady(futureResult.failed) {
        case UpstreamErrorResponse.Upstream5xxResponse(e) =>
          e.statusCode mustBe 500
        case _ =>
          fail()
      }

      application.stop()
    }

  }

}
