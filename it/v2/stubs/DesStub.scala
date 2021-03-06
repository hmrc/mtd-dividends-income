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

package v2.stubs

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.Status.OK
import support.WireMockMethods
import v2.models.requestData.DesTaxYear

object DesStub extends WireMockMethods {

  private def dividendsIncomeUrl(nino: String, taxYear: DesTaxYear): String =
    s"/income-tax/nino/$nino/income-source/dividends/annual/$taxYear"

  private val amendResponseBody =
    """
      |{"transactionReference": "12121"}
    """.stripMargin

  private val retrieveResponseBody =
    """
      |{
      |  "ukDividends": 1000.50,
      |  "otherUkDividends": 2000.35
      |}
    """.stripMargin

  def amendSuccess(nino: String, taxYear: DesTaxYear): StubMapping = {
    when(method = POST, uri = dividendsIncomeUrl(nino, taxYear))
      .thenReturn(status = OK, amendResponseBody)
  }

  def amendError(nino: String, taxYear: DesTaxYear, errorStatus: Int, errorBody: String): StubMapping = {
    when(method = POST, uri = dividendsIncomeUrl(nino, taxYear))
    .thenReturn(status = errorStatus, errorBody)
  }

  def retrieveSuccess(nino: String, taxYear: DesTaxYear): StubMapping = {
    when(method = GET, uri = dividendsIncomeUrl(nino, taxYear))
      .thenReturn(status = OK, retrieveResponseBody)
  }

  def retrieveError(nino: String, taxYear: DesTaxYear, errorStatus: Int, errorBody: String): StubMapping = {
    when(method = GET, uri = dividendsIncomeUrl(nino, taxYear))
      .thenReturn(status = errorStatus, errorBody)
  }

}
