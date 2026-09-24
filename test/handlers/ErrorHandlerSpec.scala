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

package handlers

import base.SpecBase
import org.jsoup.Jsoup

class ErrorHandlerSpec extends SpecBase {

  private lazy val handler: ErrorHandler = injector.instanceOf[ErrorHandler]

  "ErrorHandler" must {

    "render the standard error template through the main template" in {

      val result = handler.standardErrorTemplate("Title", "Heading", "Message")(fakeRequest).futureValue
      val doc    = Jsoup.parse(result.toString)

      doc.title                                  mustBe views.ViewUtils.breadcrumbTitle("Title")
      doc.getElementsByTag("h1").text            mustBe "Heading"
      doc.select("p.govuk-body").text              must include("Message")
      doc.select(".govuk-service-navigation").size must be > 0
    }
  }

}
