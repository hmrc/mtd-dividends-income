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

package v2.connectors.httpparsers

import play.api.Logger
import play.api.libs.json.{Reads, __}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import v2.models.errors.{DownstreamError, MtdError}

case class DownstreamResponse[ResponseClass](rawResponse: HttpResponse, data: Either[List[MtdError], ResponseClass])


object AmendDividendsHttpParser extends HttpParser {

  type AmendDividendsResponse = String

  val logger = Logger(this.getClass)

  private val jsonReads: Reads[String] = (__ \ "transactionReference").read[String]

  implicit val amendHttpReadsNew: HttpReads[DownstreamResponse[String]] = new HttpReads[DownstreamResponse[AmendDividendsResponse]] {

    def read(response: HttpResponse, url: String, successResponseCode: Int, valid4xxStatusCode: List[Int]): DownstreamResponse[AmendDividendsResponse] = {

      val correlationId = retrieveCorrelationId(response)
      if (response.status != successResponseCode) {
        logger.info("[AmendDividendsHttpParser][read] - " +
          s"Error response received from DES with status: ${response.status} and body\n" +
          s"${response.body} and CorrelationId: $correlationId when calling $url")
      }

      response.status match {
        case `successResponseCode` => parseResponse(response)
        case x if valid4xxStatusCode.contains(x) => DownstreamResponse(response, Left(parseDesErrors(response)))
        case catchAll => /* Log catchAll then... */ DownstreamResponse(response, Left(List(DownstreamError)))
      }

    }

    //    def parseJsonToCaseClass[A](): Either[List[MtdError], A] = ???

    def parseDesErrors(response: HttpResponse): List[MtdError] = ???

    //    {
    //      parseErrors(response) match {
    //        case v2.models.errors.SingleError(error) => List(error)
    //        case v2.models.errors.MultipleErrors(errors) => errors.toList
    //      }
    //    }

    def parseResponse(response: HttpResponse): DownstreamResponse[AmendDividendsResponse] = {
      response.validateJson[String](jsonReads) match {
        case Some(ref) => DownstreamResponse(response, Right(ref))
        case None => /* LOG */ DownstreamResponse(response, Left(List(DownstreamError)))
      }
    }

    def mapMeErrors(errors: List[MtdError], mappings: Map[String, MtdError]): List[MtdError] = {
      errors.map(error => mappings(error.code))
    }

  }
}

