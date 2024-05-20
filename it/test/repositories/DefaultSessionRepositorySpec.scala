/*
 * Copyright 2024 HM Revenue & Customs
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

package repositories

import config.FrontendAppConfig
import models.UserAnswers
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.model.Filters
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.{JsObject, Json}

import java.time.Instant
import java.time.temporal.ChronoUnit

class DefaultSessionRepositorySpec extends AnyWordSpec
  with Matchers
  with GuiceOneAppPerSuite
  with OptionValues
  with BeforeAndAfterEach
  with ScalaFutures {

  override def beforeEach(): Unit =
    sessionRepository.collection.deleteMany(BsonDocument()).toFuture().futureValue

  private val data: JsObject = Json.obj("foo" -> "bar")
  private val lastUpdated: Instant = Instant.now.minus(5, ChronoUnit.MINUTES)
  private val userAnswers: UserAnswers = UserAnswers("id", data, lastUpdated)

  val config: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
  val sessionRepository: DefaultSessionRepository = app.injector.instanceOf[DefaultSessionRepository]

  ".set" should {

    "must set the data on the supplied user answers, and save them" in {

      val expectedResult = userAnswers copy (lastUpdated = Instant.now())

      val setResult = sessionRepository.set(userAnswers).futureValue
      val updatedRecord = sessionRepository.collection.find(Filters.equal("_id", userAnswers.id)).headOption().futureValue.value

      setResult mustEqual true
      updatedRecord.id mustEqual expectedResult.id
      updatedRecord.data mustEqual expectedResult.data
    }
  }

  ".get" should {

    "when there is a record for this id" when {

      "must get the record for the input" in {
        val expectedResult = UserAnswers("id1", Json.obj("foo1" -> "bar1"))
        sessionRepository.collection.insertOne(expectedResult).toFuture().futureValue

        val result = sessionRepository.get("id1").futureValue

        result.value mustEqual expectedResult
      }
    }

    "when there is no record for this id" when {

      "must return None" in {

        sessionRepository.get("id that does not exist").futureValue must not be defined
      }
    }
  }
}
