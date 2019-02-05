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

package v2.connectors

import javax.inject.Inject
import play.api.Logger
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.logging.Authorization
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import v2.config.AppConfig
import v2.models.Dividends
import v2.models.outcomes.AmendDividendsConnectorOutcome
import v2.models.requestData.AmendDividendsRequest

import scala.concurrent.{ExecutionContext, Future}

class DesConnector @Inject()(http: HttpClient, appConfig: AppConfig) {

  val logger = Logger(this.getClass)

  private[connectors] def desHeaderCarrier(implicit hc: HeaderCarrier): HeaderCarrier = hc
    .copy(authorization = Some(Authorization(s"Bearer ${appConfig.desToken}")))
    .withExtraHeaders("Environment" -> appConfig.desEnv)

  def amend(amendDividendsRequest: AmendDividendsRequest)
           (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AmendDividendsConnectorOutcome] = {

    import v2.connectors.httpparsers.AmendDividendsHttpParser.amendHttpReads
    import v2.models.Dividends.writes

    val nino = amendDividendsRequest.nino.nino
    val taxYear = amendDividendsRequest.desTaxYear

    val url = s"${appConfig.desBaseUrl}/income-tax/nino/$nino/income-source/dividends/annual/$taxYear"

    http.POST[Dividends, AmendDividendsConnectorOutcome](url, amendDividendsRequest.model)(writes, amendHttpReads, desHeaderCarrier, implicitly)
  }
}
