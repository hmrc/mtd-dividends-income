/*
 * Copyright 2018 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIED OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package v2.endpoints

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.Status
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.libs.ws.{WSRequest, WSResponse}
import support.IntegrationBaseSpec
import v2.fixtures.Fixtures.DividendsFixture
import v2.models.errors._
import v2.models.requestData.DesTaxYear
import v2.stubs.{AuditStub, AuthStub, DesStub, MtdIdLookupStub}

class DividendsISpec extends IntegrationBaseSpec {

  private trait Test {

    val nino: String
    val taxYear: String
    val correlationId = "X-123"

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      setupStubs()
      buildRequest(s"/2.0/ni/$nino/dividends/$taxYear")
    }
  }

  "Calling the amend dividends income endpoint" should {

    "return a 204 status code" when {

      "any valid request is made" in new Test {
        override val nino: String = "AA123456A"
        override val taxYear: String = "2018-19"

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.amendSuccess(nino, DesTaxYear(taxYear))
        }

        val response: WSResponse = await(request().put(DividendsFixture.mtdFormatJson))
        response.status shouldBe Status.NO_CONTENT
      }
    }

    "return 500 (Internal Server Error)" when {

      amendErrorTest(Status.BAD_REQUEST, "INVALID_TYPE", Status.INTERNAL_SERVER_ERROR, DownstreamError)
      amendErrorTest(Status.FORBIDDEN, "NOT_FOUND_INCOME_SOURCE", Status.INTERNAL_SERVER_ERROR, DownstreamError)
      amendErrorTest(Status.INTERNAL_SERVER_ERROR, "SERVER_ERROR", Status.INTERNAL_SERVER_ERROR, DownstreamError)
      amendErrorTest(Status.SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", Status.INTERNAL_SERVER_ERROR, DownstreamError)
      amendErrorTest(Status.FORBIDDEN, "MISSING_CHARITIES_NAME_GIFT_AID", Status.INTERNAL_SERVER_ERROR, DownstreamError)
      amendErrorTest(Status.FORBIDDEN, "MISSING_GIFT_AID_AMOUNT", Status.INTERNAL_SERVER_ERROR, DownstreamError)
      amendErrorTest(Status.FORBIDDEN, "MISSING_CHARITIES_NAME_INVESTMENT", Status.INTERNAL_SERVER_ERROR, DownstreamError)
      amendErrorTest(Status.FORBIDDEN, "MISSING_INVESTMENT_AMOUNT", Status.INTERNAL_SERVER_ERROR, DownstreamError)
    }

    "return 400 (Bad Request)" when {
      amendErrorTest(Status.BAD_REQUEST, "INVALID_NINO", Status.BAD_REQUEST, NinoFormatError)
      amendErrorTest(Status.BAD_REQUEST, "INVALID_TAXYEAR", Status.BAD_REQUEST, TaxYearFormatError)
      amendErrorTest(Status.BAD_REQUEST, "INVALID_PAYLOAD", Status.BAD_REQUEST, BadRequestError)
    }

    "return a 400 (Bad Request) with multiple errors" when {

      val multipleErrors: String =
        s"""
           |{
           |	"failures" : [
           |      {
           |        "code": "INVALID_NINO",
           |        "reason": "Does not matter."
           |      },
           |      {
           |        "code": "INVALID_TAXYEAR",
           |        "reason": "Does not matter."
           |      }
           |  ]
           |}
      """.stripMargin

      s"des returns multiple errors" in new Test {
        override val nino: String = "AA123456A"
        override val taxYear: String = "2018-19"

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.amendError(nino, DesTaxYear(taxYear), BAD_REQUEST, multipleErrors)
        }

        val response: WSResponse = await(request().put(DividendsFixture.mtdFormatJson))
        response.status shouldBe Status.BAD_REQUEST
        response.json shouldBe Json.toJson(ErrorWrapper(None, BadRequestError, Some(Seq(NinoFormatError, TaxYearFormatError))))
      }

    }

    def amendErrorTest(desStatus: Int, desCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
      s"des returns an $desCode error" in new Test {
        override val nino: String = "AA123456A"
        override val taxYear: String = "2018-19"

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.amendError(nino, DesTaxYear(taxYear), desStatus, errorBody(desCode))
        }

        val response: WSResponse = await(request().put(DividendsFixture.mtdFormatJson))
        response.status shouldBe expectedStatus
        response.json shouldBe Json.toJson(expectedBody)
      }
    }

    "return 400 (Bad Request)" when {
      amendRequestValidationErrorTest("AA1123A", "2017-18", Status.BAD_REQUEST, NinoFormatError)
      amendRequestValidationErrorTest("AA123456A", "2017-17", Status.BAD_REQUEST, TaxYearFormatError)
      amendRequestValidationErrorTest("AA123456A", "2015-16", Status.BAD_REQUEST, TaxYearNotSpecifiedRuleError)
    }

    def amendRequestValidationErrorTest(requestNino:String, requestTaxYear:String, expectedStatus: Int, expectedBody: MtdError): Unit = {
      s"validation fails with ${expectedBody.code} error" in new Test {

        override val nino: String = requestNino
        override val taxYear: String = requestTaxYear

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
        }

        val response: WSResponse = await(request().put(DividendsFixture.mtdFormatJson))
        response.status shouldBe expectedStatus
        response.json shouldBe Json.toJson(expectedBody)
      }
    }
  }

  def errorBody(code: String): String =
    s"""
       |      {
       |        "code": "$code",
       |        "reason": "des message"
       |      }
      """.stripMargin

}
