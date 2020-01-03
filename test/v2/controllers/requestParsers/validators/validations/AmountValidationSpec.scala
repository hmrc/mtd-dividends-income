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

package v2.controllers.requestParsers.validators.validations

import support.UnitSpec
import v2.models.errors.{MtdError, OtherUkDividendsAmountFormatError, UkDividendsAmountFormatError}
import v2.utils.JsonErrorValidators

class AmountValidationSpec extends UnitSpec with JsonErrorValidators {

  val dummyError = MtdError("DUMMY_ERROR", "For testing only")

  "validate" should {
    "return no errors" when {
      "when a valid amount is supplied" in {
        val validAmount = Some(BigDecimal(98.76))
        val validationResult = AmountValidation.validate(validAmount, dummyError)
        validationResult.isEmpty shouldBe true
      }

      "when a zero amount is supplied" in {
        val validAmount = Some(BigDecimal(0))
        val validationResult = AmountValidation.validate(validAmount, dummyError)
        validationResult.isEmpty shouldBe true
      }

      "when the maximum allowed amount is supplied" in {
        val validAmount = Some(BigDecimal(99999999999.99))
        val validationResult = AmountValidation.validate(validAmount, dummyError)
        validationResult.isEmpty shouldBe true
      }

      "when a None is supplied" in {
        val validationResult = AmountValidation.validate(None, dummyError)
        validationResult.isEmpty shouldBe true
      }
    }

    val exampleErrorsToReturn = List(
      UkDividendsAmountFormatError,
      OtherUkDividendsAmountFormatError
    )

    exampleErrorsToReturn.foreach { error =>

      s"return a ${error.code} error" when {
        "the amount supplied exceeds the maximum allowed " in {
          val tooLargeAmount = Some(BigDecimal(999999999999.99))
          val validationResult = AmountValidation.validate(tooLargeAmount, error)
          validationResult.isEmpty shouldBe false
          validationResult.length shouldBe 1
          validationResult.head shouldBe error
        }

        "the amount is less than zero" in {
          val lessThanZero = Some(BigDecimal(-12.00))
          val validationResult = AmountValidation.validate(lessThanZero, error)
          validationResult.isEmpty shouldBe false
          validationResult.length shouldBe 1
          validationResult.head shouldBe error
        }

        "an amount greater than 2 decimal places is supplied" in {

          val greaterThanTwoDecimalPlaces = Some(BigDecimal(5000.003))
          val validationResult = AmountValidation.validate(greaterThanTwoDecimalPlaces, error)
          validationResult.isEmpty shouldBe false
          validationResult.length shouldBe 1
          validationResult.head shouldBe error
        }

      }
    }
  }
}
