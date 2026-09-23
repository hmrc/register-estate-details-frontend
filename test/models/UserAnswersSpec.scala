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

package models

import base.SpecBase
import pages.{EstateNamePage, QuestionPage}
import play.api.libs.json.{JsPath, JsResultException, Json}
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.Instant

class UserAnswersSpec extends SpecBase {

  private val instant = Instant.ofEpochMilli(1757808000000L)

  private val nestedPage = new QuestionPage[Boolean] {
    override def path: JsPath = JsPath \ "parent" \ "child"
  }

  private val unwritableData = Json.obj("parent" -> "not an object")

  "get" must {

    "return the stored answer" in {
      emptyUserAnswers.set(EstateNamePage, "Estate Name").success.value
        .get(EstateNamePage).value mustBe "Estate Name"
    }

    "return None when the page is unanswered" in {
      emptyUserAnswers.get(EstateNamePage) mustBe None
    }

    "return None when the stored value is the wrong type" in {
      UserAnswers(userAnswersId, Json.obj("estateName" -> 42)).get(EstateNamePage) mustBe None
    }
  }

  "set" must {

    "overwrite an existing value" in {
      val answers = emptyUserAnswers
        .set(EstateNamePage, "First").success.value
        .set(EstateNamePage, "Second").success.value

      answers.get(EstateNamePage).value mustBe "Second"
    }

    "fail when the path cannot be written to" in {
      val answers = UserAnswers(userAnswersId, unwritableData)

      answers.set(nestedPage, true).failure.exception mustBe a[JsResultException]
    }
  }

  "remove" must {

    "remove an answered page" in {
      emptyUserAnswers
        .set(EstateNamePage, "Estate Name").success.value
        .remove(EstateNamePage).success.value
        .get(EstateNamePage) mustBe None
    }

    "succeed when the page was never answered" in {
      emptyUserAnswers.remove(EstateNamePage).success.value.get(EstateNamePage) mustBe None
    }
    "leave the answers untouched when the path cannot be removed" in {
      val answers = UserAnswers(userAnswersId, unwritableData)

      answers.remove(nestedPage).success.value.data mustBe unwritableData
    }
  }

  "format" must {

    "round trip through the mongo representation" in {

      val answers = UserAnswers(userAnswersId, Json.obj("a" -> "b"), instant)

      val json = Json.toJson(answers)(UserAnswers.writes)

      json mustBe Json.obj(
        "_id"         -> userAnswersId,
        "data"        -> Json.obj("a" -> "b"),
        "lastUpdated" -> Json.toJson(instant)(MongoJavatimeFormats.instantWrites)
      )

      json.as(UserAnswers.reads) mustBe answers
    }
  }

}