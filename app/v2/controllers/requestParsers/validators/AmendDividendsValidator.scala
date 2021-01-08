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

package v2.controllers.requestParsers.validators

import v2.controllers.requestParsers.validators.validations._
import v2.models.Dividends
import v2.models.errors._
import v2.models.requestData.AmendDividendsRequestRawData

class AmendDividendsValidator extends Validator[AmendDividendsRequestRawData]{

  private val validationSet = List(requestUrlParamsValidations, bodyFormatValidations, emptyBodyValidation, bvrValidations)

  private def requestUrlParamsValidations: AmendDividendsRequestRawData => List[List[MtdError]] = (data: AmendDividendsRequestRawData) => {
    List(
      NinoValidation.validate(data.nino),
      TaxYearValidation.validate(data.taxYear)
    )
  }

  private def bodyFormatValidations: AmendDividendsRequestRawData => List[List[MtdError]] = (data: AmendDividendsRequestRawData) => {
    List(
      JsonFormatValidation.validate[Dividends](data.body),
      MtdTaxYearValidation.validate(data.taxYear, TaxYearNotSpecifiedRuleError)
    )
  }

  private def emptyBodyValidation: AmendDividendsRequestRawData => List[List[MtdError]] = (data: AmendDividendsRequestRawData) => {
    val dividends = data.body.json.as[Dividends]
    List(
      DefinedFieldValidation.validate(EmptyOrNonMatchingBodyRuleError, dividends.ukDividends, dividends.otherUkDividends)
    )
  }

  private def bvrValidations: AmendDividendsRequestRawData => List[List[MtdError]] = (data: AmendDividendsRequestRawData) => {

    val dividends = data.body.json.as[Dividends]

    List(
      dividends.ukDividends match {
        case None => Nil
        case amount => AmountValidation.validate(amount, UkDividendsAmountFormatError)
      },
      dividends.otherUkDividends match {
        case None => Nil
        case amount => AmountValidation.validate(amount, OtherUkDividendsAmountFormatError)
      }
    )
  }

  def validate(data: AmendDividendsRequestRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }
}
