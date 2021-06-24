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

package v2.connectors.httpparsers

import play.api.Logger
import play.api.http.Status._
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import v2.models.Dividends
import v2.models.errors.{DownstreamError, GenericError}
import v2.models.outcomes.{DesResponse, RetrieveDividendsConnectorOutcome}

object RetrieveDividendsHttpParser extends HttpParser {

  override val logger: Logger = Logger(this.getClass)

  implicit val retrieveHttpReads: HttpReads[RetrieveDividendsConnectorOutcome] = new HttpReads[RetrieveDividendsConnectorOutcome] {
    override def read(method: String, url: String, response: HttpResponse): RetrieveDividendsConnectorOutcome = {

      val correlationId = retrieveCorrelationId(response)

      if (response.status != OK) {
        logger.info("[RetrieveDividendsHttpParser][read] - " +
          s"Error response received from DES with status: ${response.status} and body\n" +
          s"${response.body} and CorrelationId: $correlationId when calling $url")
      }

      response.status match {
        case OK => logger.info("[RetrieveDividendsHttpParser][read] - " +
          s"Success response received from DES with CorrelationId: $correlationId when calling $url")
          parseResponse(response)
        case BAD_REQUEST | NOT_FOUND => Left(DesResponse(correlationId, parseErrors(response)))
        case INTERNAL_SERVER_ERROR | SERVICE_UNAVAILABLE => Left(DesResponse(correlationId, GenericError(DownstreamError)))
        case _ => Left(DesResponse(correlationId, GenericError(DownstreamError)))
      }
    }

    private def parseResponse(response: HttpResponse): RetrieveDividendsConnectorOutcome =
      response.validateJson[Dividends] match {
        case Some(dividends) => Right(DesResponse(retrieveCorrelationId(response), dividends))
        case None => Left(DesResponse(retrieveCorrelationId(response), GenericError(DownstreamError)))
      }
  }

}
