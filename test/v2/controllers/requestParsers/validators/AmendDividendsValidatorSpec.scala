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

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.AnyContentAsJson
import support.UnitSpec
import v2.fixtures.Fixtures.DividendsFixture
import v2.models.errors._
import v2.models.requestData.AmendDividendsRequestRawData

class AmendDividendsValidatorSpec extends UnitSpec{

  "running validate" should {
    "return no errors" when {
      "valid request data is supplied" in {
        val nino = "AA123456A"
        val taxYear = "2018-19"
        val expectedData = Nil
        val requestRawData = AmendDividendsRequestRawData(nino, taxYear, AnyContentAsJson(DividendsFixture.mtdFormatJson))

        val result = new AmendDividendsValidator().validate(requestRawData)

        result shouldBe expectedData
      }
    }

    "return no errors" when {
      "only ukDividends is supplied as request data" in {
        val nino = "AA123456A"
        val taxYear = "2018-19"
        val onlyUkDividendsJson: JsValue = Json.parse(
          s"""{
             |  "ukDividends": 10000.00
             |}""".stripMargin

        )
        val expectedData = Nil
        val requestRawData = AmendDividendsRequestRawData(nino, taxYear, AnyContentAsJson(onlyUkDividendsJson))

        val result = new AmendDividendsValidator().validate(requestRawData)

        result shouldBe expectedData
      }
    }

    "return no errors" when {
      "only otherUkDividends is supplied as request data" in {
        val nino = "AA123456A"
        val taxYear = "2018-19"
        val onlyOtherUkDividendsJson: JsValue = Json.parse(
          s"""{
             |  "otherUkDividends": 10000.00
             |}""".stripMargin

        )
        val expectedData = Nil
        val requestRawData = AmendDividendsRequestRawData(nino, taxYear, AnyContentAsJson(onlyOtherUkDividendsJson))

        val result = new AmendDividendsValidator().validate(requestRawData)

        result shouldBe expectedData
      }
    }

    "return nino format error" when {
      "an invalid nino is supplied" in {
        val nino = "AA1456A"
        val taxYear = "2018-19"
        val expectedData = List(NinoFormatError)
        val requestRawData = AmendDividendsRequestRawData(nino, taxYear, AnyContentAsJson(DividendsFixture.mtdFormatJson))

        val result = new AmendDividendsValidator().validate(requestRawData)

        result shouldBe expectedData
      }
    }

    "return tax year format error" when {
      "an invalid tax year is supplied" in {
        val nino = "AA123456A"
        val taxYear = "2018"
        val expectedData = List(TaxYearFormatError)
        val requestRawData = AmendDividendsRequestRawData(nino, taxYear, AnyContentAsJson(DividendsFixture.mtdFormatJson))

        val result = new AmendDividendsValidator().validate(requestRawData)

        result shouldBe expectedData
      }
    }

    "return minimum tax year rule error" when {
      "tax year is supplied is before 2016-17" in {
        val nino = "AA123456A"
        val taxYear = "2015-16"
        val expectedData = List(TaxYearNotSpecifiedRuleError)
        val requestRawData = AmendDividendsRequestRawData(nino, taxYear, AnyContentAsJson(DividendsFixture.mtdFormatJson))

        val result = new AmendDividendsValidator().validate(requestRawData)

        result shouldBe expectedData
      }
    }

    "return multiple format errors" when {
      "an invalid nino and tax year is supplied" in {
        val nino = "AA3456A"
        val taxYear = "2018"
        val expectedData = List(NinoFormatError, TaxYearFormatError)
        val requestRawData = AmendDividendsRequestRawData(nino, taxYear, AnyContentAsJson(DividendsFixture.mtdFormatJson))

        val result = new AmendDividendsValidator().validate(requestRawData)

        result shouldBe expectedData
      }
    }

    "return invalid amount error" when {
      "an invalid amount is supplied for ukDividends" in {
        val nino = "AA123456A"
        val taxYear = "2018-19"
        val requestBody:JsValue = Json.parse(
          s"""{
             |  "ukDividends": -20.00,
             |  "otherUkDividends": 10000.00
             |}""".stripMargin

        )
        val expectedData = List(UkDividendsAmountFormatError)
        val requestRawData = AmendDividendsRequestRawData(nino, taxYear, AnyContentAsJson(requestBody))

        val result = new AmendDividendsValidator().validate(requestRawData)

        result shouldBe expectedData
      }
    }

    "return empty field rule error" when {
      "an empty dividends object is supplied" in {
        val nino = "AA123456A"
        val taxYear = "2018-19"
        val requestBody:JsValue = Json.parse(
          s"""{
             |
             |}""".stripMargin

        )
        val expectedData = List(EmptyOrNonMatchingBodyRuleError)
        val requestRawData = AmendDividendsRequestRawData(nino, taxYear, AnyContentAsJson(requestBody))

        val result = new AmendDividendsValidator().validate(requestRawData)

        result shouldBe expectedData
      }
    }

    "return empty field rule error" when {
      "an incorrect dividends object is supplied" in {
        val nino = "AA123456A"
        val taxYear = "2018-19"
        val requestBody:JsValue = Json.parse(
          s"""{
             | "someField" : 0,
             | "someOtherField": 1
             |}""".stripMargin
        )
        val expectedData = List(EmptyOrNonMatchingBodyRuleError)
        val requestRawData = AmendDividendsRequestRawData(nino, taxYear, AnyContentAsJson(requestBody))

        val result = new AmendDividendsValidator().validate(requestRawData)

        result shouldBe expectedData
      }
    }

    "return multiple invalid amount errors" when {
      "an invalid amount is supplied for both ukDividends and otherUkDividends" in {
        val nino = "AA123456A"
        val taxYear = "2018-19"
        val requestBody:JsValue = Json.parse(
          s"""{
             |  "ukDividends": -20.00,
             |  "otherUkDividends": 99999999999999999999999999999999999.00
             |}""".stripMargin

        )
        val expectedData = List(UkDividendsAmountFormatError, OtherUkDividendsAmountFormatError)
        val requestRawData = AmendDividendsRequestRawData(nino, taxYear, AnyContentAsJson(requestBody))

        val result = new AmendDividendsValidator().validate(requestRawData)

        result shouldBe expectedData
      }
    }
  }
}
