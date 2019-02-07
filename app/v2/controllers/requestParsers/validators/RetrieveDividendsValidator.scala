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

import v2.controllers.requestParsers.validators.validations._
import v2.models.Dividends
import v2.models.errors.{MtdError, OtherUkDividendsAmountFormatError, TaxYearNotSpecifiedRuleError, UkDividendsAmountFormatError}
import v2.models.requestData.RetrieveDividendsRequestRawData

class RetrieveDividendsValidator extends Validator[RetrieveDividendsRequestRawData]{

  private val validationSet = List(requestUrlParamsValidations, bvrValidations)

  private def requestUrlParamsValidations: RetrieveDividendsRequestRawData => List[List[MtdError]] = (data: RetrieveDividendsRequestRawData) => {
    List(
      NinoValidation.validate(data.nino),
      TaxYearValidation.validate(data.taxYear)
    )
  }

  private def bvrValidations: RetrieveDividendsRequestRawData => List[List[MtdError]] = (data: RetrieveDividendsRequestRawData) => {
    List(
      MtdTaxYearValidation.validate(data.taxYear, TaxYearNotSpecifiedRuleError)
    )
  }

  def validate(data: RetrieveDividendsRequestRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }
}
