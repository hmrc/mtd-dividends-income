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

package v2.services

import javax.inject.Inject
import play.api.Logger
import uk.gov.hmrc.http.HeaderCarrier
import v2.models.errors._
import v2.models.requestData.{AmendDividendsRequest, RetrieveDividendsRequest}
import v2.connectors.DesConnector
import v2.models.outcomes.{AmendDividendsOutcome, DesResponse, RetrieveDividendsOutcome}

import scala.concurrent.{ExecutionContext, Future}

class DividendsService @Inject()(desConnector: DesConnector) {

  val logger: Logger = Logger(this.getClass)

  def amend(amendDividendsRequest: AmendDividendsRequest)(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext,
    correlationId: String): Future[AmendDividendsOutcome] = {

    desConnector.amend(amendDividendsRequest).map {

      case Right(desResponse) => Right(desResponse)
      case Left(DesResponse(correlationId, MultipleErrors(errors))) =>
        val mtdErrors = errors.map(error => desErrorToMtdErrorAmend(error.code))
        if (mtdErrors.contains(DownstreamError)) {
          logger.info(s"[DividendsIncomeService] [amend] [CorrelationId - $correlationId]" +
            s" - downstream returned INVALID_IDTYPE or NOT_FOUND_PERIOD. Revert to ISE")
          Left(ErrorWrapper(correlationId, DownstreamError, None))
        } else {
          Left(ErrorWrapper(correlationId, BadRequestError, Some(mtdErrors)))
        }
      case Left(DesResponse(correlationId, SingleError(error))) => Left(ErrorWrapper(correlationId, desErrorToMtdErrorAmend(error.code), None))
      case Left(DesResponse(correlationId, GenericError(error))) => Left(ErrorWrapper(correlationId, error, None))
      case Left(_@e) => Left(ErrorWrapper(e.correlationId, DownstreamError, None))
    }
  }

  def retrieve(retrieveDividendsRequest: RetrieveDividendsRequest)(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext,
    correlationId: String): Future[RetrieveDividendsOutcome] = {

    desConnector.retrieve(retrieveDividendsRequest).map {

      case Right(desResponse) => Right(DesResponse(desResponse.correlationId, desResponse.responseData))
      case Left(DesResponse(correlationId, MultipleErrors(errors))) =>
        val mtdErrors = errors.map(error => desErrorToMtdErrorRetrieve(error.code))
        if (mtdErrors.contains(DownstreamError)) {
          logger.info(s"[DividendsIncomeService] [retrieve] [CorrelationId - $correlationId]" +
            s" - downstream returned INVALID_IDTYPE, INVALID_INCOME_SOURCE or NOT_FOUND_PERIOD. Revert to ISE")
          Left(ErrorWrapper(correlationId, DownstreamError, None))
        } else {
          Left(ErrorWrapper(correlationId, BadRequestError, Some(mtdErrors)))
        }
      case Left(DesResponse(correlationId, SingleError(error))) => Left(ErrorWrapper(correlationId, desErrorToMtdErrorRetrieve(error.code), None))
      case Left(DesResponse(correlationId, GenericError(error))) => Left(ErrorWrapper(correlationId, error, None))
      case Left(_@e) => Left(ErrorWrapper(e.correlationId, DownstreamError, None))
    }
  }

  private def desErrorToMtdErrorAmend: Map[String, MtdError] = Map(
    "INVALID_NINO" -> NinoFormatError,
    "INVALID_TYPE" -> DownstreamError,
    "INVALID_TAXYEAR" -> TaxYearFormatError,
    "INVALID_PAYLOAD" -> BadRequestError,
    "NOT_FOUND_PERIOD" -> DownstreamError,
    "INVALID_ACCOUNTING_PERIOD" -> TaxYearNotSpecifiedRuleError,
    "NOT_FOUND_INCOME_SOURCE" -> DownstreamError,
    "MISSING_GIFT_AID_AMOUNT" -> DownstreamError,
    "MISSING_CHARITIES_NAME_INVESTMENT" -> DownstreamError,
    "MISSING_INVESTMENT_AMOUNT" -> DownstreamError,
    "MISSING_CHARITIES_NAME_GIFT_AID" -> DownstreamError,
    "SERVICE_UNAVAILABLE" -> DownstreamError,
    "SERVER_ERROR" -> DownstreamError
  )

  private def desErrorToMtdErrorRetrieve: Map[String, MtdError] = Map(
    "INVALID_NINO" -> NinoFormatError,
    "INVALID_TYPE" -> DownstreamError,
    "INVALID_TAXYEAR" -> TaxYearFormatError,
    "INVALID_INCOME_SOURCE" -> DownstreamError,
    "NOT_FOUND_PERIOD" -> NotFoundError,
    "NOT_FOUND_INCOME_SOURCE" -> NotFoundError,
    "SERVICE_UNAVAILABLE" -> DownstreamError,
    "SERVER_ERROR" -> DownstreamError
  )

}