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

package views

import base.SpecBase
import play.api.data.{Form, FormError}
import play.api.data.Forms.text

class ViewUtilsSpec extends SpecBase {

    "errorHref" must {

      "point at the day field for a date part error" in {
        ViewUtils.errorHref(FormError("value", "error.required", Seq("day"))) mustBe "value.day"
      }

      "point at the yes option for a yes/no field" in {
        ViewUtils.errorHref(FormError("value", "error.required"), isYesNo = true) mustBe "value-yes"
      }

      "point at the day field when the key names a date" in {
        ViewUtils.errorHref(FormError("dateOfDeath", "error.required")) mustBe "dateOfDeath.day"
      }

      "point at the field itself otherwise" in {
        ViewUtils.errorHref(FormError("estateName", "estateName.error.required")) mustBe "estateName"
      }
    }

    "isDateError" must {
      "be true for date and when" in {
        ViewUtils.isDateError("dateOfDeath") mustBe true
        ViewUtils.isDateError("whenDidThisHappen") mustBe true
      }

      "be false otherwise" in {
        ViewUtils.isDateError("estateName") mustBe false
      }
    }

    "errorPrefix" must {

      val form = Form("value" -> text)

      "be empty for a form without errors" in {
        ViewUtils.errorPrefix(form) mustBe ""
      }

      "be present for a form with a field error" in {
        ViewUtils.errorPrefix(form.withError("value", "error.required")) mustBe
          s"${messages("error.browser.title.prefix")} "
      }

      "be present for a form with a global error" in {
        ViewUtils.errorPrefix(form.withGlobalError("error.required")) mustBe
          s"${messages("error.browser.title.prefix")} "
      }
    }

    "breadcrumbTitle" must {
      "end with GOV.UK" in {
        ViewUtils.breadcrumbTitle("Test page") must endWith("GOV.UK")
      }
    }


}