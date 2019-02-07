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

package v2.controllers.requestParsers.validators

import support.UnitSpec
import v2.models.errors._
import v2.models.requestData.RetrieveDividendsRequestRawData

class RetrieveDividendsValidatorSpec extends UnitSpec{

  "running validate" should {
    "return no errors" when {
      "valid request data is supplied" in {
        val nino = "AA123456A"
        val taxYear = "2018-19"
        val expectedData = Nil
        val requestRawData = RetrieveDividendsRequestRawData(nino, taxYear)

        val result = new RetrieveDividendsValidator().validate(requestRawData)

        result shouldBe expectedData
      }
    }

    "return nino format error" when {
      "an invalid nino is supplied" in {
        val nino = "AA1456A"
        val taxYear = "2018-19"
        val expectedData = List(NinoFormatError)
        val requestRawData = RetrieveDividendsRequestRawData(nino, taxYear)

        val result = new RetrieveDividendsValidator().validate(requestRawData)

        result shouldBe expectedData
      }
    }

    "return tax year format error" when {
      "an invalid tax year is supplied" in {
        val nino = "AA123456A"
        val taxYear = "2018"
        val expectedData = List(TaxYearFormatError)
        val requestRawData = RetrieveDividendsRequestRawData(nino, taxYear)

        val result = new RetrieveDividendsValidator().validate(requestRawData)

        result shouldBe expectedData
      }
    }

    "return minimum tax year rule error" when {
      "tax year is supplied is before 2016-17" in {
        val nino = "AA123456A"
        val taxYear = "2015-16"
        val expectedData = List(TaxYearNotSpecifiedRuleError)
        val requestRawData = RetrieveDividendsRequestRawData(nino, taxYear)

        val result = new RetrieveDividendsValidator().validate(requestRawData)

        result shouldBe expectedData
      }
    }

    "return multiple format errors" when {
      "an invalid nino and tax year is supplied" in {
        val nino = "AA3456A"
        val taxYear = "2018"
        val expectedData = List(NinoFormatError, TaxYearFormatError)
        val requestRawData = RetrieveDividendsRequestRawData(nino, taxYear)

        val result = new RetrieveDividendsValidator().validate(requestRawData)

        result shouldBe expectedData
      }
    }
  }
}
