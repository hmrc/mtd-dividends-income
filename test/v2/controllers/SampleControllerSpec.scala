/*
 * Copyright 2019 HM Revenue & Customs
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

package v2.controllers

import uk.gov.hmrc.http.HeaderCarrier
import v2.mocks.services.{MockEnrolmentsAuthService, MockMtdIdLookupService}
import v2.models.errors.{NinoFormatError, UnauthorisedError}

import scala.concurrent.Future

class SampleControllerSpec extends ControllerBaseSpec {

  trait Test extends MockEnrolmentsAuthService with MockMtdIdLookupService {
    val hc = HeaderCarrier()

    lazy val target = new SampleController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService
    )
  }

  val nino = "test-nino"

  "getTaxCalculation" should {
    "return a 200" in new Test {

      MockedMtdIdLookupService.lookup(nino)
        .returns(Future.successful(Right("test-mtd-id")))

      MockedEnrolmentsAuthService.authoriseUser()

      private val result = target.doSomething(nino)(fakeGetRequest)
      status(result) shouldBe OK
      contentAsString(result) shouldBe "test-mtd-id"
    }

    "return a 400" when {
      "a invalid NI number is passed" in new Test {

        MockedMtdIdLookupService.lookup(nino)
          .returns(Future.successful(Left(NinoFormatError)))

        private val result = target.doSomething(nino)(fakeGetRequest)
        status(result) shouldBe BAD_REQUEST
      }
    }

    "return a 500" when {
      "the details passed or not authorised" in new Test {

        MockedMtdIdLookupService.lookup(nino)
          .returns(Future.successful(Left(UnauthorisedError)))

        private val result = target.doSomething(nino)(fakeGetRequest)
        status(result) shouldBe FORBIDDEN
      }
    }
  }
}
