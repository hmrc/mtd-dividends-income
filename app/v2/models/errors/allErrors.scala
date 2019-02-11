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

package v2.models.errors

// Format Errors
object NinoFormatError extends MtdError("FORMAT_NINO", "The provided NINO is invalid")
object TaxYearFormatError extends MtdError("FORMAT_TAX_YEAR", "The provided tax year is invalid")

//Standard Errors
object DownstreamError extends MtdError("INTERNAL_SERVER_ERROR", "An internal server error occurred")
object NotFoundError extends MtdError("MATCHING_RESOURCE_NOT_FOUND", "Matching resource not found")
object BadRequestError extends MtdError("INVALID_REQUEST", "Invalid request")
object BvrError extends MtdError("BUSINESS_ERROR", "Business validation error")
object ServiceUnavailableError extends MtdError("SERVICE_UNAVAILABLE", "Internal server error")

//Authorisation Errors
object UnauthorisedError extends MtdError("CLIENT_OR_AGENT_NOT_AUTHORISED", "The client and/or agent is not authorised.")

object UkDividendsAmountFormatError extends MtdError("FORMAT_UK_DIVIDENDS", "UK Dividends format is invalid.")
object OtherUkDividendsAmountFormatError extends MtdError("FORMAT_OTHER_DIVIDENDS", "Other UK Dividends format is invalid.")
object TaxYearNotSpecifiedRuleError extends
  MtdError("RULE_TAX_YEAR_NOT_SUPPORTED", "Tax year not supported, because it precedes the earliest allowable tax year")
object DividendsEmptyRuleError extends
  MtdError("RULE_EMPTY_DIVIDENDS", "A non-empty dividends object must be supplied")
