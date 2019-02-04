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

package v2.controllers.requestParsers

import play.api.mvc.AnyContentAsJson
import support.UnitSpec
import uk.gov.hmrc.domain.Nino
import v2.fixtures.Fixtures.DividendsFixture
import v2.mocks.validators.MockAmendDividendsValidator
import v2.models.errors.{BadRequestError, ErrorWrapper, NinoFormatError, TaxYearFormatError}
import v2.models.requestData.{AmendDividendsRequest, AmendDividendsRequestRawData}

class AmendDividendsRequestDataParserSpec extends UnitSpec{

  trait Test extends MockAmendDividendsValidator {
    val target = new AmendDividendsRequestDataParser(mockAmendDividendsValidator)
  }

  "Calling parse method" should {
    "return validated request data" when {
      "valid request is supplied" in new Test {

        val nino = "AA123456A"
        val taxYear = "2018-19"
        val desTaxYear = "2019"
        val expectedData = AmendDividendsRequest(Nino(nino), desTaxYear, DividendsFixture.dividendsModel)
        val requestRawData = AmendDividendsRequestRawData(nino, taxYear, AnyContentAsJson(DividendsFixture.mtdFormatJson))

        MockAmendDividendsValidator.validate(requestRawData).returns(Nil)

        private val result = target.parse(requestRawData)

        result shouldBe Right(expectedData)
      }
    }

    "return an Invalid nino error" when {
      "an invalid nino is supplied" in new Test {
        val nino = "AA456A"
        val taxYear = "2018-19"
        private val expectedData = NinoFormatError
        val requestRawData = AmendDividendsRequestRawData(nino, taxYear, AnyContentAsJson(DividendsFixture.mtdFormatJson))

        MockAmendDividendsValidator.validate(requestRawData).returns(List(expectedData))
        private val result = target.parse(requestRawData)

        result shouldBe Left(ErrorWrapper(None, expectedData, None))
      }
    }

    "return multiple errors" when {
      "an invalid nino and tax year is supplied" in new Test {
        val nino = "AA1456A"
        val taxYear = "20189"
        val expectedData = List(NinoFormatError, TaxYearFormatError)
        val requestRawData = AmendDividendsRequestRawData(nino, taxYear, AnyContentAsJson(DividendsFixture.mtdFormatJson))

        MockAmendDividendsValidator.validate(requestRawData).returns(expectedData)
        private val result = target.parse(requestRawData)

        result shouldBe Left(ErrorWrapper(None, BadRequestError, Some(expectedData)))

      }
    }

  }
}