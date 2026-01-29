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

import com.google.inject.Singleton
import config.FrontendAppConfig
import models.UserAnswers
import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.{Filters, IndexModel, IndexOptions, ReplaceOptions}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DefaultSessionRepository @Inject() (val mongo: MongoComponent, val appConfig: FrontendAppConfig)(implicit
  val ec: ExecutionContext
) extends PlayMongoRepository[UserAnswers](
      collectionName = "user-answers",
      mongoComponent = mongo,
      domainFormat = UserAnswers.userAnswersFormat,
      indexes = Seq(
        IndexModel(
          ascending("lastUpdated"),
          indexOptions = IndexOptions()
            .name("user-answers-last-updated-index")
            .expireAfter(appConfig.cacheTtl, TimeUnit.SECONDS)
        )
      ),
      replaceIndexes = appConfig.dropIndexes
    )
    with SessionRepository {

  override def get(id: String): Future[Option[UserAnswers]] =
    collection.find(Filters.equal("_id", id)).headOption()

  override def set(userAnswers: UserAnswers): Future[Boolean] = {

    val updatedAnswers = userAnswers copy (lastUpdated = Instant.now())
    val options        = ReplaceOptions().upsert(true)

    collection
      .replaceOne(filter = Filters.equal("_id", updatedAnswers.id), replacement = updatedAnswers, options = options)
      .toFuture()
      .map(_.wasAcknowledged())
  }

}

trait SessionRepository {

  def get(id: String): Future[Option[UserAnswers]]

  def set(userAnswers: UserAnswers): Future[Boolean]
}
