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

import forms.EstateNameFormProvider
import models.NormalMode
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.QuestionViewBehaviours
import views.html.EstateNameView
import play.api.i18n.{Lang, Messages}

class EstateNameViewSpec extends QuestionViewBehaviours[String] {

  val messageKeyPrefix = "estateName"
  val name             = "Name"

  override val form: Form[String] = new EstateNameFormProvider().apply()

  "Name view" must {

    val view = viewFor[EstateNameView](Some(emptyUserAnswers))

    def applyView(form: Form[_]): HtmlFormat.Appendable =
      view.apply(form, NormalMode)(fakeRequest, messages)

    behave like normalPage(applyView(form), messageKeyPrefix)

    behave like pageWithBackLink(applyView(form))

    "fields" must {

      behave like pageWithTextFields(
        form,
        applyView,
        messageKeyPrefix,
        None,
        "",
        "value"
      )
    }

    "rendered in Welsh" must {

      val welsh: Messages = messagesApi.preferred(Seq(Lang("cy")))

      val doc = asDocument(view.apply(form, NormalMode)(fakeRequest, welsh))

      "set the character count language to Welsh" in {
        doc.select(".govuk-character-count").attr("data-language") mustBe "cy"
      }

      "render the Welsh character count hint" in {
        doc.select(".govuk-character-count__message").text must include("Gallwch nodi hyd at 53")
      }

      "render the i18n attributes" in {
        val element = doc.select(".govuk-character-count").first()

        element.attr("data-i18n.characters-under-limit.one")  must not be empty
        element.attr("data-i18n.characters-over-limit.other") must not be empty
      }
    }

    behave like pageWithASubmitButton(applyView(form))
  }

}
