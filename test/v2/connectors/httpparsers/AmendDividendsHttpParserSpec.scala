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
import v2.models.errors._
import v2.models.outcomes.DesResponse

class AmendDividendsHttpParserSpec extends UnitSpec {

  val method = "PUT"
  val url = "test-url"

  val transactionReference = "000000000001"
  val desExpectedJson: JsValue = Json.obj("transactionReference" -> transactionReference)
  val desResponse = DesResponse("X-123", transactionReference)

  val correlationId = "X-123"

  "read" should {
    "return a DesResponse" when {
      "the http response contains a 200" in {

        val httpResponse = HttpResponse(OK, desExpectedJson.toString(), Map("CorrelationId" -> Seq(correlationId)))

        val result = AmendDividendsHttpParser.amendHttpReads.read(PUT, "/test", httpResponse)
        result shouldBe Right(desResponse)
      }
    }

    "return a single error" when {
      "the http response contains a 400 with an error response body" in {
        val errorResponseJson = Json.parse(
          """
            |{
            |  "code": "INVALID_NINO",
            |  "reason": "some reason"
            |}
          """.stripMargin)
        val expected = DesResponse(correlationId, SingleError(MtdError("INVALID_NINO", "some reason")))

        val httpResponse = HttpResponse(BAD_REQUEST, errorResponseJson.toString(), Map("CorrelationId" -> Seq(correlationId)))
        val result = AmendDividendsHttpParser.amendHttpReads.read(PUT, "/test", httpResponse)
        result shouldBe Left(expected)
      }

      "the http response contains a 403 with an error response body" in {
        val errorResponseJson = Json.parse(
          """
            |{
            |  "code": "MISSING_GIFT_AID_AMOUNT",
            |  "reason": "some reason"
            |}
          """.stripMargin)
        val expected = DesResponse(correlationId, SingleError(MtdError("MISSING_GIFT_AID_AMOUNT", "some reason")))

        val httpResponse = HttpResponse(FORBIDDEN, errorResponseJson.toString(), Map("CorrelationId" -> Seq(correlationId)))
        val result = AmendDividendsHttpParser.amendHttpReads.read(PUT, "/test", httpResponse)
        result shouldBe Left(expected)
      }
    }

    "return a generic error" when {

      "DES returns status 200 and body can't be read" in {

        val httpResponse = HttpResponse(OK, Json.obj("foo" -> "bar").toString(), Map("CorrelationId" -> Seq(correlationId)))
        val expected = DesResponse(correlationId, GenericError(DownstreamError))


        val result = AmendDividendsHttpParser.amendHttpReads.read(PUT, "/test", httpResponse)
        result shouldBe Left(expected)
      }

      "the error response from DES can't be read" in {
        val errorResponseJson = Json.parse(
          """
            |{
            |  "foo": "TEST_CODE",
            |  "bar": "some reason"
            |}
          """.
            stripMargin)
        val expected = DesResponse(correlationId, GenericError(DownstreamError))

        val httpResponse = HttpResponse(BAD_REQUEST, errorResponseJson.toString(), Map("CorrelationId" -> Seq(correlationId)))
        val result = AmendDividendsHttpParser.amendHttpReads.read(PUT, "/test", httpResponse)
        result shouldBe Left(expected)
      }

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
        val result = AmendDividendsHttpParser.amendHttpReads.read(PUT, "/test", httpResponse)
        result shouldBe Left(expected)
      }

      "the http response contains a 500 with an error response body" in {
        val errorResponseJson = Json.parse(
          """
            |{
            |  "code": "SERVER_ERROR",
            |  "reason": "some reason"
            |}
          """.
            stripMargin)
        val expected = DesResponse(correlationId, GenericError(DownstreamError))

        val httpResponse = HttpResponse(INTERNAL_SERVER_ERROR, errorResponseJson.toString(), Map("CorrelationId" -> Seq(correlationId)))
        val result = AmendDividendsHttpParser.amendHttpReads.read(PUT, "/test", httpResponse)
        result shouldBe Left(expected)
      }

      "the http response contains a 503 with an error response body" in {
        val errorResponseJson = Json.parse(
          """
            |{
            |  "code": "SERVICE_UNAVAILABLE",
            |  "reason": "some reason"
            |}
          """.
            stripMargin)
        val expected = DesResponse(correlationId, GenericError(DownstreamError))

        val httpResponse = HttpResponse(SERVICE_UNAVAILABLE, errorResponseJson.toString(), Map("CorrelationId" -> Seq(correlationId)))
        val result = AmendDividendsHttpParser.amendHttpReads.read(PUT, "/test", httpResponse)
        result shouldBe Left(expected)
      }
    }

    "return multiple errors" when {
      "the http response contains a 400 with an error response body with multiple errors" in {
        val errorResponseJson = Json.parse(
          """
            |{
            |	"failures" : [
            |    {
            |      "code": "INVALID_NINO",
            |      "reason": "some reason"
            |    },
            |    {
            |      "code": "INVALID_TAXYEAR",
            |      "reason": "some reason"
            |    }
            |  ]
            |}
          """.stripMargin)
        val expected = DesResponse(correlationId, MultipleErrors(Seq(MtdError("INVALID_NINO", "some reason"), MtdError("INVALID_TAXYEAR", "some reason"))))

        val httpResponse = HttpResponse(BAD_REQUEST, errorResponseJson.toString(), Map("CorrelationId" -> Seq(correlationId)))
        val result = AmendDividendsHttpParser.amendHttpReads.read(POST, "/test", httpResponse)
        result shouldBe Left(expected)
      }

      "the http response contains a 403 with an error response body with multiple errors" in {
        val errorResponseJson = Json.parse(
          """
            |{
            |	"failures" : [
            |    {
            |      "code": "NOT_FOUND_PERIOD",
            |      "reason": "some reason"
            |    },
            |    {
            |      "code": "MISSING_GIFT_AID_AMOUNT",
            |      "reason": "some reason"
            |    }
            |  ]
            |}
          """.stripMargin)
        val expected = DesResponse(correlationId,
          MultipleErrors(Seq(MtdError("NOT_FOUND_PERIOD", "some reason"), MtdError("MISSING_GIFT_AID_AMOUNT", "some reason"))))

        val httpResponse = HttpResponse(FORBIDDEN, errorResponseJson.toString(), Map("CorrelationId" -> Seq(correlationId)))
        val result = AmendDividendsHttpParser.amendHttpReads.read(POST, "/test", httpResponse)
        result shouldBe Left(expected)
      }
    }
  }
}
