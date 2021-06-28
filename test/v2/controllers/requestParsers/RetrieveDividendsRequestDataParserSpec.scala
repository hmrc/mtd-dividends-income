/*
 * Copyright 2021 HM Revenue & Customs
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

package v2.controllers.requestParsers

import support.UnitSpec
import v2.mocks.validators.MockRetrieveDividendsValidator
import v2.models.domain.Nino
import v2.models.errors.{BadRequestError, ErrorWrapper, NinoFormatError, TaxYearFormatError}
import v2.models.requestData.{DesTaxYear, RetrieveDividendsRequest, RetrieveDividendsRequestRawData}

class RetrieveDividendsRequestDataParserSpec extends UnitSpec {

  implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  trait Test extends MockRetrieveDividendsValidator {
    val parser = new RetrieveDividendsRequestDataParser(mockRetrieveDividendsValidator)
  }

  "Calling parse" should {
    "return valid retrieve request object" when {
      "valid request data is supplied" in new Test {
        val nino: String = "AA123456A"
        val taxYear: String = "2017-18"
        val inputRawRequest: RetrieveDividendsRequestRawData = RetrieveDividendsRequestRawData(nino, taxYear)
        val expectedResult: RetrieveDividendsRequest = RetrieveDividendsRequest(Nino(nino), DesTaxYear.fromMtd(taxYear))

        MockRetrieveDividendsValidator.validate(inputRawRequest).returns(Nil)

        private val result = parser.parse(inputRawRequest)

        result shouldBe Right(expectedResult)
      }
    }

    "return single validation nino format error" when {
      "invalid nino and valid tax year is supplied" in new Test {
        val nino: String = "AA1256A"
        val taxYear: String = "2017-18"
        val inputRawRequest: RetrieveDividendsRequestRawData = RetrieveDividendsRequestRawData(nino, taxYear)
        val expectedResult: ErrorWrapper = ErrorWrapper(correlationId, NinoFormatError, None)

        MockRetrieveDividendsValidator.validate(inputRawRequest).returns(List(NinoFormatError))
        private val result = parser.parse(inputRawRequest)

        result shouldBe Left(expectedResult)
      }
    }

    "return single validation tax year format error" when {
      "invalid tax year and valid nino is supplied" in new Test {
        val nino: String = "AA123456A"
        val taxYear: String = "2017"
        val inputRawRequest: RetrieveDividendsRequestRawData = RetrieveDividendsRequestRawData(nino, taxYear)
        val expectedResult: ErrorWrapper = ErrorWrapper(correlationId, TaxYearFormatError, None)

        MockRetrieveDividendsValidator.validate(inputRawRequest).returns(List(TaxYearFormatError))
        private val result = parser.parse(inputRawRequest)

        result shouldBe Left(expectedResult)
      }
    }

    "return multiple validation errors" when {
      "invalid tax year and nino is supplied" in new Test {
        val nino: String = "AA1256A"
        val taxYear: String = "2017"
        val inputRawRequest: RetrieveDividendsRequestRawData = RetrieveDividendsRequestRawData(nino, taxYear)
        val expectedResult: ErrorWrapper = ErrorWrapper(correlationId, BadRequestError, Some(List(NinoFormatError, TaxYearFormatError)))

        MockRetrieveDividendsValidator.validate(inputRawRequest).returns(List(NinoFormatError, TaxYearFormatError))
        private val result = parser.parse(inputRawRequest)

        result shouldBe Left(expectedResult)
      }
    }
  }
}