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

package controllers

import base.SpecBase
import connectors.EstateConnector
import models.NormalMode
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class IndexControllerSpec extends SpecBase {

  val mockEstateConnector: EstateConnector = mock[EstateConnector]

  val name: String = "Estate Name"

  "Index Controller" must {

    "return OK and the correct view for a GET" in {

      when(mockEstateConnector.getCorrespondenceName()(any(), any())).thenReturn(Future.successful(Some(name)))

      val application =
        applicationBuilder(userAnswers = None)
          .overrides(bind[EstateConnector].toInstance(mockEstateConnector))
          .build()

      val request = FakeRequest(GET, routes.IndexController.onPageLoad.url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result) mustBe Some(controllers.routes.EstateNameController.onPageLoad(NormalMode).url)

      application.stop()
    }
  }
}
