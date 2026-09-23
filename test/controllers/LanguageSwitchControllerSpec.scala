/*
 * Copyright 2026 HM Revenue & Customs
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
import play.api.mvc.Headers
import play.api.test.FakeRequest
import play.api.test.Helpers._

class LanguageSwitchControllerSpec extends SpecBase {

  private def switchLanguageRoute(lang: String): String = routes.LanguageSwitchController.switchToLanguage(lang).url

  private val fakeUrl: String = "fakeUrl"

  private def switchTo(lang: String, referer: Option[String] = Some(fakeUrl)) = {
    val application = applicationBuilder().build()

    val headers = Headers(referer.map("Referer" -> _).toSeq: _*)
    val request = FakeRequest(GET, switchLanguageRoute(lang)).withHeaders(headers)

    val result  = route(application, request).value
    val outcome =
      (status(result), redirectLocation(result).value, cookies(result).find(_.name == "PLAY_LANG").map(_.value))

    application.stop()
    outcome
  }

  "LanguageSwitch Controller" when {

    "English selected" must {
      "switch to English and return to the referring page" in {
        switchTo("english") mustBe (SEE_OTHER, fakeUrl, Some("en"))
      }
    }

    "Welsh selected" must {
      "switch to Welsh and return to the referring page" in {
        switchTo("cymraeg") mustBe (SEE_OTHER, fakeUrl, Some("cy"))
      }
    }

    "an unrecognised language is selected" must {
      "fall back to English" in {
        switchTo("klingon") mustBe (SEE_OTHER, fakeUrl, Some("en"))
      }
    }

    "no referer in header" must {
      "redirect to the login continue url" in {
        switchTo("cymraeg", referer = None) mustBe
          (SEE_OTHER, frontendAppConfig.loginContinueUrl, Some("cy"))
      }
    }
  }

}
