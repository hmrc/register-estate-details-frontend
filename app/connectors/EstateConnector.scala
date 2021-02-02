/*
 * Copyright 2021 HM Revenue & Customs
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

import config.FrontendAppConfig
import javax.inject.Inject
import models.EstateDetails
import play.api.libs.json.{JsString, JsSuccess, JsValue}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

class EstateConnector @Inject()(http: HttpClient, config : FrontendAppConfig) {

  private val correspondenceNameUrl = s"${config.estatesUrl}/estates/correspondence/name"

  def addCorrespondenceName(estateName: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    http.POST[JsValue, HttpResponse](correspondenceNameUrl, JsString(estateName))
  }

  def getCorrespondenceName()(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[String]] = {
    http.GET[JsValue](correspondenceNameUrl) flatMap {
      _.validate[EstateDetails] match {
        case JsSuccess(details, _) => Future.successful(Some(details.name))
        case _ => Future.successful(None)
      }
    }
  }

}
