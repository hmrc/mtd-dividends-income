/*
 * Copyright 2020 HM Revenue & Customs
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

package v2.services

import uk.gov.hmrc.domain.Nino
import v2.fixtures.Fixtures.DividendsFixture
import v2.mocks.connectors.MockDesConnector
import v2.models.errors._
import v2.models.outcomes.DesResponse
import v2.models.requestData.{DesTaxYear, RetrieveDividendsRequest}

import scala.concurrent.Future

class RetrieveDividendsServiceSpec extends ServiceSpec {

  val correlationId = "X-123"
  val nino = "AA123456A"

  trait Test extends MockDesConnector {
    val service = new DividendsService(connector)
  }

  "calling retrieve" should {
    "return valid dividends data" when {
      "a valid retrieve is passed" in new Test{
        val desTaxYear = "2019"
        val expectedResult = DesResponse(correlationId, DividendsFixture.dividendsModel)
        val retrieveDividendsRequest =  RetrieveDividendsRequest(Nino(nino), DesTaxYear(desTaxYear))
        val expectedDesResponse = DesResponse(correlationId, DividendsFixture.dividendsModel)

        MockDesConnector.retrieve(retrieveDividendsRequest).returns(Future.successful(Right(expectedDesResponse)))

        val result = await(service.retrieve(retrieveDividendsRequest))

        result shouldBe Right(expectedResult)
      }
    }

    "return single invalid tax year error" when {
      "an invalid tax year is passed" in new Test {
        val taxYear = "2019-20"
        val expectedResult = ErrorWrapper(Some(correlationId), TaxYearFormatError, None)
        val retrieveDividendsRequest =  RetrieveDividendsRequest(Nino(nino), DesTaxYear.fromMtd(taxYear))

        MockDesConnector.retrieve(retrieveDividendsRequest).
          returns(Future.successful(Left(DesResponse(correlationId, SingleError(MtdError("INVALID_TAXYEAR", "reason"))))))

        val result = await(service.retrieve(retrieveDividendsRequest))

        result shouldBe Left(expectedResult)
      }
    }

    "return multiple errors" when {
      "the DesConnector returns multiple errors" in new Test {
        val taxYear = "2019-20"
        val retrieveDividendsRequest =  RetrieveDividendsRequest(Nino(nino), DesTaxYear.fromMtd(taxYear))
        val response = DesResponse(correlationId,
          MultipleErrors(Seq(MtdError("INVALID_NINO", "reason"), MtdError("INVALID_TAXYEAR", "reason"))))

        MockDesConnector.retrieve(retrieveDividendsRequest).returns(Future.successful(Left(response)))

        val expected = ErrorWrapper(Some(correlationId), BadRequestError, Some(Seq(NinoFormatError, TaxYearFormatError)))

        private val result = await(service.retrieve(retrieveDividendsRequest))
        result shouldBe Left(expected)
      }
    }
    
    "return a single error" when {
      "the DesConnector returns multiple errors and one maps to a DownstreamError" in new Test {
        val desTaxYear = "2019"
        val retrieveDividendsRequest =  RetrieveDividendsRequest(Nino(nino), DesTaxYear(desTaxYear))

        val response = DesResponse(correlationId,
          MultipleErrors(Seq(MtdError("INVALID_NINO", "reason"), MtdError("INVALID_TYPE", "reason"))))
        MockDesConnector.retrieve(retrieveDividendsRequest).returns(Future.successful(Left(response)))

        val expected = ErrorWrapper(Some(correlationId), DownstreamError, None)

        private val result = await(service.retrieve(retrieveDividendsRequest))
        result shouldBe Left(expected)
      }
    }

    "the DesConnector returns a GenericError" in new Test {
      val desTaxYear = "2019"
      val retrieveDividendsRequest =  RetrieveDividendsRequest(Nino(nino), DesTaxYear(desTaxYear))
      val response = DesResponse(correlationId, GenericError(DownstreamError))
      MockDesConnector.retrieve(retrieveDividendsRequest).returns(Future.successful(Left(response)))

      val expected = ErrorWrapper(Some(correlationId), DownstreamError, None)

      private val result = await(service.retrieve(retrieveDividendsRequest))
      result shouldBe Left(expected)
    }

    val errorMap: Map[String, MtdError] = Map(
      "INVALID_NINO" -> NinoFormatError,
      "INVALID_TYPE" -> DownstreamError,
      "INVALID_TAXYEAR" -> TaxYearFormatError,
      "INVALID_INCOME_SOURCE" -> DownstreamError,
      "NOT_FOUND_PERIOD" -> NotFoundError,
      "NOT_FOUND_INCOME_SOURCE" -> NotFoundError,
      "SERVICE_UNAVAILABLE" -> DownstreamError,
      "SERVER_ERROR" -> DownstreamError
    )

    for (error <- errorMap.keys) {

      s"the DesConnector returns a single $error error" in new Test {
        val desTaxYear = "2019"
        val retrieveDividendsRequest =  RetrieveDividendsRequest(Nino(nino), DesTaxYear(desTaxYear))

        val response = DesResponse(correlationId, SingleError(MtdError(error, "reason")))

        val expected = ErrorWrapper(Some(correlationId), errorMap(error), None)

        MockDesConnector.retrieve(retrieveDividendsRequest).returns(Future.successful(Left(response)))

        private val result = await(service.retrieve(retrieveDividendsRequest))
        result shouldBe Left(expected)
      }

    }


  }


}
