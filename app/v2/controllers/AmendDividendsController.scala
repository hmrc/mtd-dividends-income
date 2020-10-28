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

package v2.controllers

import cats.data.EitherT
import cats.implicits._
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContentAsJson, ControllerComponents, _}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import v2.controllers.requestParsers.AmendDividendsRequestDataParser
import v2.models.audit.{AuditError, AuditEvent, AuditResponse, DividendsIncomeAuditDetail}
import v2.models.auth.UserDetails
import v2.models.errors._
import v2.models.requestData.AmendDividendsRequestRawData
import v2.services.{AuditService, DividendsService, EnrolmentsAuthService, MtdIdLookupService}
import v2.utils.IdGenerator

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AmendDividendsController @Inject()(val authService: EnrolmentsAuthService,
                                         val lookupService: MtdIdLookupService,
                                         dividendsService: DividendsService,
                                         amendDividendsRequestDataParser: AmendDividendsRequestDataParser,
                                         auditService: AuditService,
                                         cc: ControllerComponents,
                                         val idGenerator: IdGenerator)(implicit ec: ExecutionContext)
  extends AuthorisedController(cc) with BaseController {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "AmendDividendsController", endpointName = "amend")

  def amend(nino: String, taxYear: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>

      implicit val correlationId: String = idGenerator.generateCorrelationId
      logger.info(
        s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
          s"with CorrelationId: $correlationId")

      val rawData = AmendDividendsRequestRawData(nino, taxYear, AnyContentAsJson(request.body))
      val result =
        for {
          parsedRequest <- EitherT.fromEither[Future](amendDividendsRequestDataParser.parse(rawData))
          correlationId <- EitherT(dividendsService.amend(parsedRequest))
        } yield {
          logger.info(
            s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
              s"Success response received with CorrelationId: ${correlationId}")
          auditSubmission(createAuditDetails(nino, taxYear, NO_CONTENT, request.request.body, correlationId, request.userDetails))

          NoContent
            .withApiHeaders(correlationId)
        }

      result.leftMap { errorWrapper =>
        val resCorrelationId = errorWrapper.correlationId
        val result = processError(errorWrapper).withHeaders("X-CorrelationId" -> resCorrelationId)
        logger.info(
          s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
            s"Error response received with CorrelationId: $resCorrelationId")

        auditSubmission(createAuditDetails(nino, taxYear,
          result.header.status, request.request.body, resCorrelationId, request.userDetails, Some(errorWrapper)))
        result
      }.merge
    }

  private def processError(errorWrapper: ErrorWrapper) = {
    errorWrapper.error match {
      case BadRequestError
           | NinoFormatError
           | TaxYearFormatError
           | TaxYearNotSpecifiedRuleError
           | UkDividendsAmountFormatError
           | OtherUkDividendsAmountFormatError
           | RuleTaxYearRangeExceededError
           | EmptyOrNonMatchingBodyRuleError => BadRequest(Json.toJson(errorWrapper))
      case NotFoundError => NotFound(Json.toJson(errorWrapper))
      case DownstreamError => InternalServerError(Json.toJson(errorWrapper))
      case _ => InternalServerError(Json.toJson(DownstreamError))
    }
  }

  private def createAuditDetails(nino: String,
                                 taxYear: String,
                                 statusCode: Int,
                                 dividends: JsValue,
                                 correlationId: String,
                                 userDetails: UserDetails,
                                 errorWrapper: Option[ErrorWrapper] = None): DividendsIncomeAuditDetail = {

    val auditResponse = errorWrapper.map { x =>
      AuditResponse(statusCode, x.allErrors.map(e => AuditError(e.code)))
    }

    DividendsIncomeAuditDetail(
      userType = userDetails.userType,
      agentReferenceNumber = userDetails.agentReferenceNumber,
      nino = nino,
      taxYear = taxYear,
      request = dividends,
      `X-CorrelationId` = correlationId,
      response = auditResponse)
  }

  private def auditSubmission(details: DividendsIncomeAuditDetail)
                             (implicit ec: ExecutionContext,
                              hc: HeaderCarrier): Future[AuditResult] = {
    val event = AuditEvent("updateDividendsAnnualSummary", "update-dividends-annual-summary", details)
    auditService.auditEvent(event)
  }
}