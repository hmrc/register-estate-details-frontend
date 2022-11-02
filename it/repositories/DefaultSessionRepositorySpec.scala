package repositories

import models.UserAnswers
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.model.Filters
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{BeforeAndAfter, OptionValues}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.Json
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import java.time.LocalDateTime

class DefaultSessionRepositorySpec
  extends AnyFreeSpec
    with GuiceOneAppPerSuite
    with Matchers
    with DefaultPlayMongoRepositorySupport[UserAnswers]
    with ScalaFutures
    with IntegrationPatience
    with OptionValues
    with MockitoSugar
    with BeforeAndAfter {

  override def beforeEach(): Unit =
    repository.collection.deleteMany(BsonDocument()).toFuture()

  private val userAnswers = UserAnswers("id", Json.obj("foo" -> "bar"), LocalDateTime.now.minusMinutes(5))

  protected override val repository: DefaultSessionRepository = app.injector.instanceOf[DefaultSessionRepository]

  ".set" - {

    "must set the data on the supplied user answers, and save them" in {

      val expectedResult = userAnswers copy (lastUpdated = LocalDateTime.now())

      val setResult     = repository.set(userAnswers).futureValue
      val updatedRecord = find(Filters.equal("_id", userAnswers.id)).futureValue.headOption.value

      setResult mustEqual true
      updatedRecord.id mustEqual expectedResult.id
      updatedRecord.data mustEqual expectedResult.data
    }
  }

  ".get" - {

    "when there is a record for this id" - {

      "must get the record for the input" in {
        val expectedResult = UserAnswers("id1", Json.obj("foo1" -> "bar1"))
        insert(expectedResult).futureValue

        val result         = repository.get("id1").futureValue

        result.value mustEqual expectedResult
      }
    }

    "when there is no record for this id" - {

      "must return None" in {

        repository.get("id that does not exist").futureValue must not be defined
      }
    }
  }

}
