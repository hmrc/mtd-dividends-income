
package v3.services

import javax.inject.Inject
import play.api.Logger
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads}
import v3.connectors.{DesConnector, DesUri}
//import v2.connectors.DesConnector
import v2.models.errors._
import v2.models.outcomes.AmendDividendsOutcome
import v2.models.requestData.AmendDividendsRequest
import v3.connectors.DesConnectorOutcome

import scala.concurrent.{ExecutionContext, Future}

class AmendDividendsService @Inject()(desConnector: DesConnector) {

  val logger: Logger = Logger(this.getClass)

//  def amend[HttpReadType](amendDividendsRequest: AmendDividendsRequest)(implicit hc: HeaderCarrier,
//                                                                        ec: ExecutionContext,
//                                                                        httpReads: HttpReads[HttpReadType])
//  : Future[DesConnectorOutcome[AmendDividendsOutcome]] = {
//
////    import AmendDividendsRequest._
////
////    desConnector.post[AmendDividendsRequest, HttpReadType](
////      body = amendDividendsRequest,
////      desUri = DesUri("")
////    )
//
//  }


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


}
