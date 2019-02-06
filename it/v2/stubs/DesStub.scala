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

package v2.stubs

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.Status.OK
import support.WireMockMethods

object DesStub extends WireMockMethods {

  private def dividendsIncomeUrl(nino: String, taxYear: String): String =
    s"/income-tax/nino/$nino/income-source/dividends/annual/$taxYear"

  private val amendResponseBody =
    """
      |{"transactionReference": "12121"}
    """.stripMargin

  def amendSuccess(nino: String, taxYear: String): StubMapping = {
    when(method = POST, uri = dividendsIncomeUrl(nino, taxYear))
      .thenReturn(status = OK, amendResponseBody)
  }

  def amendError(nino: String, taxYear: String, errorStatus: Int, errorBody: String): StubMapping = {
    when(method = POST, uri = dividendsIncomeUrl(nino, taxYear))
    .thenReturn(status = errorStatus, errorBody)
  }

}