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

import org.scalatest.OneInstancePerTest
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContentAsJson, Result}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import v2.fixtures.Fixtures.DividendsFixture
import v2.mocks.requestParsers.{MockAmendDividendsRequestDataParser, MockRetrieveDividendsRequestDataParser}
import v2.mocks.services.{MockAuditService, MockDividendsService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import v2.models.Dividends
import v2.models.audit._
import v2.models.errors._
import v2.models.requestData.{AmendDividendsRequest, AmendDividendsRequestRawData, DesTaxYear}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class DividendsControllerAmendSpec extends ControllerBaseSpec
  with MockEnrolmentsAuthService
  with MockMtdIdLookupService
  with MockDividendsService
  with MockAmendDividendsRequestDataParser
  with MockRetrieveDividendsRequestDataParser
  with MockAuditService
  with OneInstancePerTest {

  trait Test {

    val hc = HeaderCarrier()

    val target = new DividendsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      mockDividendsService,
      mockAmendDividendsRequestDataParser,
      mockRetrieveDividendsRequestDataParser,
      mockAuditService,
      cc
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
  }

  val nino = "AA123456A"
  val taxYear = "2017-18"
  val correlationId = "X-123"
  val amendDividendsRequest: AmendDividendsRequest =
    AmendDividendsRequest(Nino(nino), DesTaxYear.fromMtd(taxYear), DividendsFixture.dividendsModel)

  val auditRequest = new Dividends(Some(500.25), Some(100.25))

  "amend" should {
    "return a successful response with X-CorrelationId in the header" when {
      "the request received is valid" in new Test {

        MockAmendDividendsRequestDataParser.parse(
          AmendDividendsRequestRawData(nino, taxYear, AnyContentAsJson(DividendsFixture.mtdFormatJson)))
          .returns(Right(amendDividendsRequest))

        MockDividendsService.amend(amendDividendsRequest)
          .returns(Future.successful(Right(correlationId)))

        val result: Future[Result] = target.amend(nino, taxYear)(fakePostRequest(DividendsFixture.mtdFormatJson))
        status(result) shouldBe NO_CONTENT
        header("X-CorrelationId", result) shouldBe Some(correlationId)

        val auditDetail = new DividendsIncomeAuditDetail("Individual", None,
          nino, taxYear, DividendsFixture.mtdFormatJson, correlationId)

        val auditEvent = new AuditEvent[DividendsIncomeAuditDetail]("updateDividendsAnnualSummary",
          "update-dividends-annual-summary", auditDetail)

        MockedAuditService.verifyAuditEvent(auditEvent).once
      }
    }

    "return single error response with status 400" when {
      "the request received failed the validation" in new Test() {

        MockAmendDividendsRequestDataParser.parse(
          AmendDividendsRequestRawData(nino, taxYear, AnyContentAsJson(DividendsFixture.mtdFormatJson)))
          .returns(Left(ErrorWrapper(None, NinoFormatError, None)))

        val result: Future[Result] = target.amend(nino, taxYear)(fakePostRequest(DividendsFixture.mtdFormatJson))
        status(result) shouldBe BAD_REQUEST
        header("X-CorrelationId", result) should not be empty

        MockedAuditService.verifyAuditEvent(auditEventFor(NinoFormatError, Status.BAD_REQUEST,
          header("X-CorrelationId", result).get, DividendsFixture.mtdFormatJson)).once
      }
    }

    "return a 400 Bad Request with a single error" when {

      val badRequestErrorsFromParser = List(
        BadRequestError,
        NinoFormatError,
        TaxYearFormatError,
        TaxYearNotSpecifiedRuleError,
        UkDividendsAmountFormatError,
        OtherUkDividendsAmountFormatError
      )

      val badRequestErrorsFromService = List(
        NinoFormatError,
        TaxYearFormatError,
        BadRequestError
      )

      badRequestErrorsFromParser.foreach(errorsFromParserTester(_, BAD_REQUEST))
      badRequestErrorsFromService.foreach(errorsFromServiceTester(_, BAD_REQUEST))

    }

    "return a 500 Internal Server Error with a single error" when {

      val internalServerErrorErrors = List(
        DownstreamError
      )

      internalServerErrorErrors.foreach(errorsFromParserTester(_, INTERNAL_SERVER_ERROR))
      internalServerErrorErrors.foreach(errorsFromServiceTester(_, INTERNAL_SERVER_ERROR))

    }

    "return a valid error response" when {
      "multiple errors exist" in new Test() {
        val amendDividendsRequestData =
          AmendDividendsRequestRawData(nino, taxYear, AnyContentAsJson(DividendsFixture.mtdFormatJson))
        val multipleErrorResponse = ErrorWrapper(Some(correlationId), BadRequestError, Some(Seq(NinoFormatError, TaxYearFormatError)))

        MockAmendDividendsRequestDataParser.parse(amendDividendsRequestData)
          .returns(Left(multipleErrorResponse))

        val response: Future[Result] = target.amend(nino, taxYear)(fakePostRequest[JsValue](DividendsFixture.mtdFormatJson))

        status(response) shouldBe BAD_REQUEST
        contentAsJson(response) shouldBe Json.toJson(multipleErrorResponse)
        header("X-CorrelationId", response) shouldBe Some(correlationId)

        val auditResponse = new AuditResponse(Status.BAD_REQUEST, Seq(AuditError("FORMAT_NINO"), AuditError("FORMAT_TAX_YEAR")))

        val auditErrorDetail = new DividendsIncomeAuditDetail("Individual", None,
          nino, taxYear, DividendsFixture.mtdFormatJson, correlationId, Some(auditResponse))

        val auditEvent = new AuditEvent[DividendsIncomeAuditDetail]("updateDividendsAnnualSummary",
          "update-dividends-annual-summary", auditErrorDetail)

        MockedAuditService.verifyAuditEvent(auditEvent).once
      }
    }
  }

  def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
    s"a ${error.code} error is returned from the parser" in new Test {

      val amendDividendsRequestRawData =
        AmendDividendsRequestRawData(nino, taxYear, AnyContentAsJson(DividendsFixture.mtdFormatJson))

      MockAmendDividendsRequestDataParser.parse(amendDividendsRequestRawData)
        .returns(Left(ErrorWrapper(Some(correlationId), error, None)))

      val response: Future[Result] = target.amend(nino, taxYear)(fakePostRequest[JsValue](DividendsFixture.mtdFormatJson))

      status(response) shouldBe expectedStatus
      contentAsJson(response) shouldBe Json.toJson(error)
      header("X-CorrelationId", response) shouldBe Some(correlationId)

      MockedAuditService.verifyAuditEvent(auditEventFor(error, expectedStatus, correlationId, DividendsFixture.mtdFormatJson)).once
    }
  }

  def errorsFromServiceTester(error: MtdError, expectedStatus: Int): Unit = {
    s"a ${error.code} error is returned from the service" in new Test {

      val amendDividendsRequestRawData =
        AmendDividendsRequestRawData(nino, taxYear, AnyContentAsJson(DividendsFixture.mtdFormatJson))

      MockAmendDividendsRequestDataParser.parse(amendDividendsRequestRawData)
        .returns(Right(amendDividendsRequest))

      MockDividendsService.amend(amendDividendsRequest)
        .returns(Future.successful(Left(ErrorWrapper(Some(correlationId), error, None))))

      val response: Future[Result] = target.amend(nino, taxYear)(fakePostRequest[JsValue](DividendsFixture.mtdFormatJson))

      status(response) shouldBe expectedStatus
      contentAsJson(response) shouldBe Json.toJson(error)
      header("X-CorrelationId", response) shouldBe Some(correlationId)

      MockedAuditService.verifyAuditEvent(auditEventFor(error, expectedStatus, correlationId, DividendsFixture.mtdFormatJson)).once
    }
  }

  private def auditEventFor(error: MtdError, expectedStatus: Int,
                            correlationId: String,
                            request: JsValue) = {
    val auditResponse = new AuditResponse(expectedStatus, Seq(AuditError(s"${error.code}")))

    val auditErrorDetail = new DividendsIncomeAuditDetail("Individual", None,
      nino, taxYear, request, correlationId, Some(auditResponse))

    new AuditEvent[DividendsIncomeAuditDetail]("updateDividendsAnnualSummary",
      "update-dividends-annual-summary", auditErrorDetail)
  }
}

