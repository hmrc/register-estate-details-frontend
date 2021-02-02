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

package controllers

import base.SpecBase
import connectors.{EstateConnector, EstatesStoreConnector}
import forms.EstateNameFormProvider
import models.NormalMode
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import pages.EstateNamePage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse
import views.html.EstateNameView

import scala.concurrent.Future

class EstateNameControllerSpec extends SpecBase with MockitoSugar {

  val form = new EstateNameFormProvider().apply()
  val name = "Name"

  private lazy val nameRoute = routes.EstateNameController.onPageLoad(NormalMode).url
  private lazy val submitDetailsRoute = routes.EstateNameController.onSubmit(NormalMode).url
  private lazy val completedRoute = "http://localhost:8822/register-an-estate/registration-progress"

  "Name Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, nameRoute)

      val view = application.injector.instanceOf[EstateNameView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, NormalMode)(request, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val ua = emptyUserAnswers.set(EstateNamePage, name)
      val application = applicationBuilder(userAnswers = Some(ua.success.value)).build()

      val request = FakeRequest(GET, nameRoute)

      val view = application.injector.instanceOf[EstateNameView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(name), NormalMode)(request, messages).toString

      application.stop()
    }

    "redirect to the 'estate hub overview' page when submitted" in {

      val ua = emptyUserAnswers.set(EstateNamePage, name).success.value

      val mockEstateConnector = mock[EstateConnector]
      val mockEstatesStoreConnector = mock[EstatesStoreConnector]

      val application =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(bind[EstateConnector].toInstance(mockEstateConnector))
          .overrides(bind[EstatesStoreConnector].toInstance(mockEstatesStoreConnector))
          .build()

      when(mockEstateConnector.addCorrespondenceName(any())(any(), any())).thenReturn(Future.successful(HttpResponse(OK, "")))
      when(mockEstatesStoreConnector.setTaskComplete()(any(), any())).thenReturn(Future.successful(HttpResponse(OK, "")))

      val request = FakeRequest(POST, submitDetailsRoute).withFormUrlEncodedBody(("value", name))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual completedRoute

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request =
        FakeRequest(POST, nameRoute)
          .withFormUrlEncodedBody(("value", ""))

      val boundForm = form.bind(Map("value" -> ""))

      val view = application.injector.instanceOf[EstateNameView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, NormalMode)(request, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, nameRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, nameRoute)
          .withFormUrlEncodedBody(("firstName", "value 1"), ("lastName", "value 2"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }
  }
}
