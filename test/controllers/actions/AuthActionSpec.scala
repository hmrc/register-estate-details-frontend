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

package controllers.actions

import base.SpecBase
import com.google.inject.Inject
import controllers.routes
import play.api.mvc.{Action, AnyContent, BodyParsers, Results}
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AffinityGroup.Organisation
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.http.{HeaderCarrier, UnauthorizedException}

import scala.concurrent.{ExecutionContext, Future}

class AuthActionSpec extends SpecBase {

  class Harness(authAction: IdentifierAction) {
    def onPageLoad(): Action[AnyContent] = authAction(_ => Results.Ok)
  }

  implicit private class HelperOps[A](a: A) {
    def ~[B](b: B) = new ~(a, b)
  }

  private type AuthRetrievals = Future[Option[String] ~ Option[AffinityGroup]]

  private def authRetrievals(
    internalId: Option[String],
    affinityGroup: Option[AffinityGroup]
  ): AuthRetrievals =
    Future.successful(internalId ~ affinityGroup)

  private def actionToTest(authConnector: AuthConnector) =
    new AuthenticatedIdentifierAction(authConnector, frontendAppConfig, injector.instanceOf[BodyParsers.Default])(
      ExecutionContext.Implicits.global
    )

  private def resultFor(authConnector: AuthConnector) =
    new Harness(actionToTest(authConnector)).onPageLoad()(fakeRequest)

  "Auth Action" when {

    "the user is authenticated" must {

      "invoke the block with an identifier request" in {

        val result = resultFor(new FakeAuthConnector(authRetrievals(Some("internalId"), Some(Organisation))))

        status(result) mustBe OK
      }
    }

    "the internal id cannot be retrieved" must {

      "fail with an unauthorized exception" in {

        val result = resultFor(new FakeAuthConnector(authRetrievals(None, Some(Organisation))))

        whenReady(result.failed) { e =>
          e mustBe an[UnauthorizedException]
        }
      }
    }

    "the user hasn't logged in" must {

      "redirect the user to log in " in {

        val result = resultFor(new FakeFailingAuthConnector(new MissingBearerToken))

        status(result) mustBe SEE_OTHER

        redirectLocation(result).get must startWith(frontendAppConfig.loginUrl)
      }
    }

    "the user's session has expired" must {

      "redirect the user to log in " in {

        val result = resultFor(new FakeFailingAuthConnector(new BearerTokenExpired))

        status(result) mustBe SEE_OTHER

        redirectLocation(result).get must startWith(frontendAppConfig.loginUrl)
      }
    }

    "the user doesn't have sufficient enrolments" must {

      "redirect the user to the unauthorised page" in {

        val result = resultFor(new FakeFailingAuthConnector(new InsufficientEnrolments))

        status(result) mustBe SEE_OTHER

        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad.url)
      }
    }

    "the user doesn't have sufficient confidence level" must {

      "redirect the user to the unauthorised page" in {

        val result = resultFor(new FakeFailingAuthConnector(new InsufficientConfidenceLevel))

        status(result) mustBe SEE_OTHER

        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad.url)
      }
    }

    "the user used an unaccepted auth provider" must {

      "redirect the user to the unauthorised page" in {

        val result = resultFor(new FakeFailingAuthConnector(new UnsupportedAuthProvider))

        status(result) mustBe SEE_OTHER

        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad.url)
      }
    }

    "the user has an unsupported affinity group" must {

      "redirect the user to the unauthorised page" in {

        val result = resultFor(new FakeFailingAuthConnector(new UnsupportedAffinityGroup))

        status(result) mustBe SEE_OTHER

        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad.url)
      }
    }

    "the user has an unsupported credential role" must {

      "redirect the user to the unauthorised page" in {

        val result = resultFor(new FakeFailingAuthConnector(new UnsupportedCredentialRole))

        status(result) mustBe SEE_OTHER

        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad.url)
      }
    }
  }

}

class FakeFailingAuthConnector @Inject() (exceptionToReturn: Throwable) extends AuthConnector {
  val serviceUrl: String = ""

  override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[A] =
    Future.failed(exceptionToReturn)

}

class FakeAuthConnector(stubbedRetrievalResult: Future[_]) extends AuthConnector {

  override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[A] =
    stubbedRetrievalResult.map(_.asInstanceOf[A])

}
