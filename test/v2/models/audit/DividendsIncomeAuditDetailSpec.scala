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

package v2.models.audit

import play.api.http.Status
import play.api.libs.json.Json
import support.UnitSpec
import v2.fixtures.Fixtures.DividendsFixture

class DividendsIncomeAuditDetailSpec extends UnitSpec {

  "write" should {
    "return a valid json" when {
      "valid dividends audit detail object with only required fields is supplied" in {

        val json = Json.parse(
          """
            | {
            |     "userType": "Organisation",
            |     "nino": "MA123456D",
            |     "taxYear": "2017",
            |     "request":{
            |         "ukDividends": 500.25,
            |         "otherUkDividends": 100.25
            |     },
            |     "X-CorrelationId": "X-123"
            | }
          """.stripMargin)

        val auditDetail = new DividendsIncomeAuditDetail("Organisation", None,
        "MA123456D", "2017", DividendsFixture.mtdFormatJson, "X-123")

        Json.toJson(auditDetail) shouldBe json
      }
    }

    "return a valid json" when {
      "valid dividends audit detail object with all fields is supplied" in {

        val json = Json.parse(
          """
            | {
            |     "userType": "Agent",
            |     "agentReferenceNumber": "012345678",
            |     "nino": "MA123456D",
            |     "taxYear": "2017",
            |     "request":{
            |         "ukDividends": 500.25,
            |         "otherUkDividends": 100.25
            |     },
            |     "response": {
            |       "httpStatus": 400,
            |       "errors": [
            |         {
            |          "errorCode": "FORMAT_NINO"
            |        }
            |       ]
            |     },
            |     "X-CorrelationId": "X-123"
            | }
          """.stripMargin)

        val auditResponse = new AuditResponse(Status.BAD_REQUEST, Seq(AuditError("FORMAT_NINO")))

        val auditDetail = new DividendsIncomeAuditDetail("Agent", Some("012345678"),
          "MA123456D", "2017", DividendsFixture.mtdFormatJson, "X-123", Some(auditResponse))

        Json.toJson(auditDetail) shouldBe json
      }
    }
  }
}
