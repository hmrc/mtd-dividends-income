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

import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers._
import support.UnitSpec
import uk.gov.hmrc.http.HttpResponse
import v2.fixtures.Fixtures.DividendsFixture
import v2.models.errors.{DownstreamError, GenericError, MtdError, SingleError}
import v2.models.outcomes.DesResponse

class RetrieveDividendsHttpParserSpec extends UnitSpec {

  val method = "GET"
  val url = "test-url"

  val transactionReference = "000000000001"
  val desExpectedJson: JsValue = DividendsFixture.mtdFormatJson

  val desResponse = DesResponse("X-123", DividendsFixture.dividendsModel)

  val correlationId = "X-123"

  "read" should {
    "return a DesResponse with dividends" when {
      "the http response contains a 200" in {

        val httpResponse = HttpResponse(OK, desExpectedJson.toString(), Map("CorrelationId" -> Seq(correlationId)))

        val result = RetrieveDividendsHttpParser.retrieveHttpReads.read(GET, "/test", httpResponse)
        result shouldBe Right(desResponse)
      }
    }
    "return a single error" when {
      "the http response contains a 400 with an error response body" in {
        val errorResponseJson = Json.parse(
          """
            |{
            |  "code": "TEST_CODE",
            |  "reason": "some reason"
            |}
          """.stripMargin)
        val expected = DesResponse(correlationId, SingleError(MtdError("TEST_CODE", "some reason")))

        val httpResponse = HttpResponse(BAD_REQUEST, errorResponseJson.toString(), Map("CorrelationId" -> Seq(correlationId)))
        val result = RetrieveDividendsHttpParser.retrieveHttpReads.read(GET, "/", httpResponse)
        result shouldBe Left(expected)
      }

      "the http response contains a 404 with an error response body" in {
        val errorResponseJson = Json.parse(
          """
            |{
            |  "code": "TEST_CODE",
            |  "reason": "some reason"
            |}
          """.stripMargin)
        val expected = DesResponse(correlationId, SingleError(MtdError("TEST_CODE", "some reason")))

        val httpResponse = HttpResponse(NOT_FOUND, errorResponseJson.toString(), Map("CorrelationId" -> Seq(correlationId)))
        val result = RetrieveDividendsHttpParser.retrieveHttpReads.read(GET, "/", httpResponse)
        result shouldBe Left(expected)
      }
    }

    "return a generic error" when {

      "the error response status code is not one that is handled" in {
        val errorResponseJson = Json.parse(
          """
            |{
            |  "foo": "TEST_CODE",
            |  "bar": "some reason"
            |}
          """.
            stripMargin)
        val expected = DesResponse(correlationId, GenericError(DownstreamError))
        val unHandledStatusCode = SEE_OTHER

        val httpResponse = HttpResponse(unHandledStatusCode, errorResponseJson.toString(), Map("CorrelationId" -> Seq(correlationId)))
        val result = RetrieveDividendsHttpParser.retrieveHttpReads.read(PUT, "/test", httpResponse)
        result shouldBe Left(expected)
      }


      "the error response from DES can't be read" in {
        val expected = DesResponse(correlationId, GenericError(DownstreamError))

        val httpResponse = HttpResponse(OK, "", Map("CorrelationId" -> Seq(correlationId)))
        val result = RetrieveDividendsHttpParser.retrieveHttpReads.read(PUT, "/test", httpResponse)
        result shouldBe Left(expected)
      }

      "the http response contains a 500 with an error response body" in {
        val errorResponseJson = Json.parse(
          """
            |{
            |  "code": "TEST_CODE",
            |  "reason": "some reason"
            |}
          """.
            stripMargin)
        val expected = DesResponse(correlationId, GenericError(DownstreamError))

        val httpResponse = HttpResponse(INTERNAL_SERVER_ERROR, errorResponseJson.toString(), Map("CorrelationId" -> Seq(correlationId)))
        val result = RetrieveDividendsHttpParser.retrieveHttpReads.read(PUT, "/test", httpResponse)
        result shouldBe Left(expected)
      }

      "the http response contains a 503 with an error response body" in {
        val errorResponseJson = Json.parse(
          """
            |{
            |  "code": "TEST_CODE",
            |  "reason": "some reason"
            |}
          """.
            stripMargin)
        val expected = DesResponse(correlationId, GenericError(DownstreamError))

        val httpResponse = HttpResponse(SERVICE_UNAVAILABLE, errorResponseJson.toString(), Map("CorrelationId" -> Seq(correlationId)))
        val result = RetrieveDividendsHttpParser.retrieveHttpReads.read(PUT, "/test", httpResponse)
        result shouldBe Left(expected)
      }

    }

  }

}
