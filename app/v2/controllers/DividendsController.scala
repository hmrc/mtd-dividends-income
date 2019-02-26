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

package v2.controllers

import java.util.UUID

import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, AnyContentAsJson, ControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import v2.controllers.requestParsers.{AmendDividendsRequestDataParser, RetrieveDividendsRequestDataParser}
import v2.models.audit
import v2.models.audit.{AuditEvent, AuditRequest}
import v2.models.auth.UserDetails
import v2.models.errors._
import v2.models.requestData.{AmendDividendsRequest, AmendDividendsRequestRawData, RetrieveDividendsRequestRawData}
import v2.services.{AuditService, DividendsService, EnrolmentsAuthService, MtdIdLookupService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DividendsController @Inject()(val authService: EnrolmentsAuthService,
                                    val lookupService: MtdIdLookupService,
                                    dividendsService: DividendsService,
                                    amendDividendsRequestDataParser: AmendDividendsRequestDataParser,
                                    retrieveDividendsRequestDataParser: RetrieveDividendsRequestDataParser,
                                    auditService: AuditService,
                                    cc: ControllerComponents
                                   ) extends AuthorisedController(cc) {

  val logger: Logger = Logger(this.getClass)

  def amend(nino: String, taxYear: String): Action[JsValue] = authorisedAction(nino).async(parse.json) { implicit request =>

    amendDividendsRequestDataParser.parse(AmendDividendsRequestRawData(nino, taxYear, AnyContentAsJson(request.body))) match {
      case Right(amendDividendsRequest) => dividendsService.amend(amendDividendsRequest).map{
        case Right(correlationId) =>
          //auditSubmission(amendDividendsRequest, correlationId)
          logger.info(s"[DividendsController][amend] - Success response received with CorrelationId: $correlationId")
          NoContent.withHeaders("X-CorrelationId" -> correlationId)
        case Left(errorWrapper) => processError(errorWrapper).withHeaders("X-CorrelationId" -> getCorrelationId(errorWrapper))
      }
      case Left(errorWrapper) => Future.successful {
        processError(errorWrapper).withHeaders("X-CorrelationId" -> getCorrelationId(errorWrapper))
      }
    }
  }

  def retrieve(nino: String, taxYear: String): Action[AnyContent] = authorisedAction(nino).async { implicit request =>

    retrieveDividendsRequestDataParser.parse(RetrieveDividendsRequestRawData(nino, taxYear)) match {
      case Right(retrievedDividendsRequest) => dividendsService.retrieve(retrievedDividendsRequest).map{
        case Right(desResponse) =>

          logger.info(s"[DividendsController][retrieve] - Success response received with CorrelationId: ${desResponse.correlationId}")
          Ok(Json.toJson(desResponse.responseData)).withHeaders("X-CorrelationId" -> desResponse.correlationId)
        case Left(errorWrapper) => processError(errorWrapper).withHeaders("X-CorrelationId" -> getCorrelationId(errorWrapper))
      }
      case Left(errorWrapper) => Future.successful {
        processError(errorWrapper).withHeaders("X-CorrelationId" -> getCorrelationId(errorWrapper))
      }
    }
  }

  private def processError(errorWrapper: ErrorWrapper) = {
    errorWrapper.error match {
      case BadRequestError
           | NinoFormatError
           | TaxYearFormatError
           | TaxYearNotSpecifiedRuleError
           | UkDividendsAmountFormatError
           | OtherUkDividendsAmountFormatError
           | EmptyOrNonMatchingBodyRuleError => BadRequest(Json.toJson(errorWrapper))
      case NotFoundError => NotFound(Json.toJson(errorWrapper))
      case DownstreamError => InternalServerError(Json.toJson(errorWrapper))

    }
  }

  private def getCorrelationId(errorWrapper: ErrorWrapper): String = {
    errorWrapper.correlationId match {
      case Some(correlationId) => logger.info("[DividendsController][getCorrelationId] - " +
        s"Error received from DES ${Json.toJson(errorWrapper)} with CorrelationId: $correlationId")
        correlationId
      case None =>
        val correlationId = UUID.randomUUID().toString
        logger.info("[DividendsController][getCorrelationId] - " +
          s"Validation error: ${Json.toJson(errorWrapper)} with CorrelationId: $correlationId")
        correlationId
    }
  }

  private def auditSubmission(amendDividendsRequest: AmendDividendsRequest ,
                                        correlationId: String)
                                       (implicit ec: ExecutionContext,
                                        hc: HeaderCarrier,
                                        userDetails: UserDetails): Future[AuditResult] = {
    val auditRequest = new AuditRequest(amendDividendsRequest.model.ukDividends.get,
      amendDividendsRequest.model.otherUkDividends.get)

    val details = audit.DividendsIncomeAuditDetail(
      userType = userDetails.userType,
      agentReferenceNumber = userDetails.agentReferenceNumber,
      nino = amendDividendsRequest.nino.toString(),
      taxYear = amendDividendsRequest.desTaxYear,
      request = auditRequest,
      `X-CorrelationId` = correlationId
    )

    val event = AuditEvent("updateDividendsAnnualSummary", "update-dividends-annual-summary", details)

    auditService.auditEvent(event)
  }
}




