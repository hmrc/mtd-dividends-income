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
import v2.models.requestData.{AmendDividendsRequest, DesTaxYear}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AmendDividendsServiceSpec extends ServiceSpec {

  val correlationId = "X-123"
  val nino = "AA123456A"

  trait Test extends MockDesConnector {
    val service = new DividendsService(connector)
  }

  "calling amend" should {
    "return a valid CorrelationId" when {
      "a valid request is passed" in new Test{
        val desTaxYear = "2019"
        val expectedResult = correlationId
        val amendDividendsRequest = AmendDividendsRequest(Nino(nino), DesTaxYear(desTaxYear), DividendsFixture.dividendsModel)
        MockDesConnector.amend(amendDividendsRequest).returns(Future.successful(Right(DesResponse(correlationId, "ref"))))

        val result = await(service.amend(amendDividendsRequest))

        result shouldBe Right(expectedResult)
      }
    }

    "return single invalid tax year error" when {
      "an invalid tax year is passed" in new Test {
        val taxYear = "2019-20"
        val expectedResult = ErrorWrapper(Some(correlationId), TaxYearFormatError, None)
        val amendDividendsRequest = AmendDividendsRequest(Nino(nino), DesTaxYear.fromMtd(taxYear), DividendsFixture.dividendsModel)

        MockDesConnector.amend(amendDividendsRequest).
          returns(Future.successful(Left(DesResponse(correlationId, SingleError(MtdError("INVALID_TAXYEAR", "reason"))))))

        val result = await(service.amend(amendDividendsRequest))

        result shouldBe Left(expectedResult)
      }
    }

    "return multiple errors" when {
      "the DesConnector returns multiple errors" in new Test {
        val taxYear = "2019-20"
        val expectedResult = correlationId
        val amendDividendsRequest = AmendDividendsRequest(Nino(nino), DesTaxYear.fromMtd(taxYear), DividendsFixture.dividendsModel)
        val response = DesResponse(correlationId,
          MultipleErrors(Seq(MtdError("INVALID_NINO", "reason"), MtdError("INVALID_TAXYEAR", "reason"))))

        MockDesConnector.amend(amendDividendsRequest).returns(Future.successful(Left(response)))

        val expected = ErrorWrapper(Some(correlationId), BadRequestError, Some(Seq(NinoFormatError, TaxYearFormatError)))

        private val result = await(service.amend(amendDividendsRequest))
        result shouldBe Left(expected)
      }

    }
    "return a single error" when {
      "the DesConnector returns multiple errors and one maps to a DownstreamError" in new Test {
        val desTaxYear = "2019"
        val expectedResult = correlationId
        val amendDividendsRequest = AmendDividendsRequest(Nino(nino), DesTaxYear(desTaxYear), DividendsFixture.dividendsModel)

        val response = DesResponse(correlationId,
          MultipleErrors(Seq(MtdError("INVALID_NINO", "reason"), MtdError("INVALID_TYPE", "reason"))))
        MockDesConnector.amend(amendDividendsRequest).returns(Future.successful(Left(response)))

        val expected = ErrorWrapper(Some(correlationId), DownstreamError, None)

        private val result = await(service.amend(amendDividendsRequest))
        result shouldBe Left(expected)
      }
    }

    "the DesConnector returns a GenericError" in new Test {
      val desTaxYear = "2019"
      val expectedResult = correlationId
      val amendDividendsRequest = AmendDividendsRequest(Nino(nino), DesTaxYear(desTaxYear), DividendsFixture.dividendsModel)
      val response = DesResponse(correlationId, GenericError(DownstreamError))
      MockDesConnector.amend(amendDividendsRequest).returns(Future.successful(Left(response)))

      val expected = ErrorWrapper(Some(correlationId), DownstreamError, None)

      private val result = await(service.amend(amendDividendsRequest))
      result shouldBe Left(expected)
    }

    val errorMap: Map[String, MtdError] = Map(
      "INVALID_NINO" -> NinoFormatError,
      "INVALID_TYPE" -> DownstreamError,
      "INVALID_TAXYEAR" -> TaxYearFormatError,
      "INVALID_PAYLOAD" -> BadRequestError,
      "MISSING_CHARITIES_NAME_GIFT_AID" -> DownstreamError,
      "MISSING_GIFT_AID_AMOUNT" -> DownstreamError,
      "MISSING_CHARITIES_NAME_INVESTMENT" -> DownstreamError,
      "MISSING_INVESTMENT_AMOUNT" -> DownstreamError,
      "SERVER_ERROR" -> DownstreamError,
      "SERVICE_UNAVAILABLE" -> DownstreamError
    )


    for (error <- errorMap.keys) {

      s"the DesConnector returns a single $error error" in new Test {
        val desTaxYear = "2019"
        val expectedResult: String = correlationId
        val amendDividendsRequest = AmendDividendsRequest(Nino(nino), DesTaxYear(desTaxYear), DividendsFixture.dividendsModel)

        val response = DesResponse(correlationId, SingleError(MtdError(error, "reason")))

        val expected = ErrorWrapper(Some(correlationId), errorMap(error), None)

        MockDesConnector.amend(amendDividendsRequest).returns(Future.successful(Left(response)))

        private val result = await(service.amend(amendDividendsRequest))
        result shouldBe Left(expected)
      }

    }

  }


}
