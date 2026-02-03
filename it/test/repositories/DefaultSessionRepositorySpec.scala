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

package repositories

import config.FrontendAppConfig
import models.UserAnswers
import org.mongodb.scala.bson.{BsonDateTime, BsonDocument, BsonString}
import org.mongodb.scala.model.Filters
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.{JsObject, Json}

import java.time.Instant
import java.time.temporal.ChronoUnit

class DefaultSessionRepositorySpec
    extends AnyWordSpec
    with Matchers
    with GuiceOneAppPerSuite
    with OptionValues
    with BeforeAndAfterEach
    with ScalaFutures {

  val config: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
  val sessionRepository: DefaultSessionRepository = app.injector.instanceOf[DefaultSessionRepository]
  val insertDataCollection =
    sessionRepository.mongo.database
      .getCollection[BsonDocument]("user-answers")
  private val data: JsObject = Json.obj("foo" -> "bar")
  private val lastUpdated: Instant = Instant.now.minus(5, ChronoUnit.MINUTES)
  private val userAnswers: UserAnswers = UserAnswers("id", data, lastUpdated)

  override def beforeEach(): Unit =
    sessionRepository.collection.deleteMany(BsonDocument()).toFuture().futureValue

  ".set" should {

    "must set the data on the supplied user answers, and save them" in {

      val expectedResult = userAnswers copy (lastUpdated = Instant.now())

      val setResult     = sessionRepository.set(userAnswers).futureValue
      val updatedRecord =
        sessionRepository.collection.find(Filters.equal("_id", userAnswers.id)).headOption().futureValue.value

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

  "getAllInvalidDateDocuments" should {

    "return ids for docs where lastUpdated exists but is NOT a BSON DateTime" in {

      insertDataCollection.insertOne(
        BsonDocument(
          "_id" -> BsonString("id-string-lastUpdated"),
          "data" -> BsonDocument("foo1" -> BsonString("bar1")),
          "lastUpdated" -> BsonString("2026-01-30T13:06:19.192Z")
        )
      ).toFuture().futureValue

      val result =
        sessionRepository.getAllInvalidDateDocuments(limit = 100).toFuture().futureValue

      result must contain("id-string-lastUpdated")
    }

    "NOT return ids for docs where lastUpdated is a BSON DateTime" in {
      insertDataCollection.insertOne(BsonDocument(
        "_id" -> BsonString("id1"),
        "data" -> BsonDocument("foo" -> BsonString("bar")),
        "lastUpdated" -> BsonDateTime(Instant.now().toEpochMilli)
      ))

      val ids = sessionRepository.getAllInvalidDateDocuments(limit = 100).toFuture().futureValue
      ids must not contain ("id1")
    }

  }

  "updateAllInvalidDateDocuments" should {
    "return matched=0 and updated=0 when no ids exist" in {
      val counters = sessionRepository.updateAllInvalidDateDocuments(Seq("no-id")).futureValue

      counters.errors mustBe 0
      counters.matched mustBe 0
      counters.updated mustBe 0
    }
  }


}
