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

package v2.connectors

import uk.gov.hmrc.domain.Nino
import v2.fixtures.Fixtures.DividendsFixture
import v2.mocks.{MockAppConfig, MockHttpClient}
import v2.models.Dividends
import v2.models.errors.{MultipleErrors, NinoFormatError, SingleError, TaxYearFormatError}
import v2.models.outcomes.{AmendDividendsConnectorOutcome, DesResponse, RetrieveDividendsConnectorOutcome}
import v2.models.requestData.{AmendDividendsRequest, DesTaxYear, RetrieveDividendsRequest}

import scala.concurrent.Future

class DesConnectorSpec extends ConnectorSpec {

  lazy val baseUrl = "test-BaseUrl"

  trait Test extends MockHttpClient with MockAppConfig {
    val connector = new DesConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )
    MockedAppConfig.desBaseUrl returns baseUrl
    MockedAppConfig.desToken returns "des-token"
    MockedAppConfig.desEnvironment returns "des-environment"
  }


  "calling amend" should {
    "return successful response" when {
      "valid data is supplied" in new Test {
        val nino: String = "AA123456A"
        val desTaxYear: String = "2019"
        val amendDividendsRequest: AmendDividendsRequest = AmendDividendsRequest(Nino(nino), DesTaxYear(desTaxYear), DividendsFixture.dividendsModel)
        val transactionReference: String = "000000000001"
        val expectedResult: DesResponse[String] = DesResponse[String](correlationId, transactionReference)

        MockedHttpClient.post[Dividends, AmendDividendsConnectorOutcome](
          s"$baseUrl" + s"/income-tax/nino/$nino/income-source/dividends/annual/$desTaxYear",
          DividendsFixture.dividendsModel)
          .returns(Future.successful(Right(expectedResult)))

        val result: AmendDividendsConnectorOutcome = await(connector.amend(amendDividendsRequest))
        result shouldBe Right(expectedResult)
      }
    }

    "return error response with CorrelationId and tax year format error" when {
      "the request supplied has invalid tax year" in new Test {

        val expectedDesResponse: DesResponse[SingleError] = DesResponse(correlationId, SingleError(TaxYearFormatError))
        val nino: String = "AA123456A"
        val taxYear: String = "2018-19"

        MockedHttpClient.post[Dividends, AmendDividendsConnectorOutcome](
          s"$baseUrl/income-tax/nino/$nino/income-source/dividends/annual/${DesTaxYear.fromMtd(taxYear)}",
          DividendsFixture.dividendsModel)
          .returns(Future.successful(Left(expectedDesResponse)))

        val amendDividendsRequest: AmendDividendsRequest = AmendDividendsRequest(Nino(nino), DesTaxYear.fromMtd(taxYear), DividendsFixture.dividendsModel)

        val result: AmendDividendsConnectorOutcome = await(connector.amend(amendDividendsRequest))

        result shouldBe Left(expectedDesResponse)
      }
    }

    "return response with multiple errors and CorrelationId" when {
      "an request supplied with invalid tax year and invalid Nino " in new Test {

        val expectedDesResponse: DesResponse[MultipleErrors] = DesResponse(correlationId, MultipleErrors(Seq(TaxYearFormatError, NinoFormatError)))
        val nino: String = "AA123456A"
        val taxYear: String = "2018-19"

        MockedHttpClient.post[Dividends, AmendDividendsConnectorOutcome](
          s"$baseUrl/income-tax/nino/$nino/income-source/dividends/annual/${DesTaxYear.fromMtd(taxYear)}",
          DividendsFixture.dividendsModel)
          .returns(Future.successful(Left(expectedDesResponse)))

        val amendDividendsRequest: AmendDividendsRequest = AmendDividendsRequest(Nino(nino), DesTaxYear.fromMtd(taxYear), DividendsFixture.dividendsModel)

        val result: AmendDividendsConnectorOutcome = await(connector.amend(amendDividendsRequest))

        result shouldBe Left(expectedDesResponse)
      }
    }
  }


  "calling retrieve" should {
    "return successful response" when {
      "valid data is supplied" in new Test {

        val nino: String = "AA123456A"
        val desTaxYear: String = "2019"
        val expectedResult: DesResponse[Dividends] = DesResponse(correlationId, DividendsFixture.dividendsModel)

        MockedHttpClient.get[RetrieveDividendsConnectorOutcome](
          s"$baseUrl" + s"/income-tax/nino/$nino/income-source/dividends/annual/$desTaxYear")
          .returns(Future.successful(Right(expectedResult)))

        val retrieveDividends: RetrieveDividendsRequest = RetrieveDividendsRequest(Nino(nino), DesTaxYear(desTaxYear))

        val result: RetrieveDividendsConnectorOutcome = await(connector.retrieve(retrieveDividends))

        result shouldBe Right(expectedResult)

      }
    }
    "return an error response with CorrelationId" when {
      "an request supplied with invalid tax year" in new Test {

        val expectedDesResponse: DesResponse[SingleError] = DesResponse("X-123", SingleError(TaxYearFormatError))
        val nino: String = "AA123456A"
        val taxYear: String = "1111-12"

        MockedHttpClient.get[RetrieveDividendsConnectorOutcome](
          s"$baseUrl" + s"/income-tax/nino/$nino/income-source/dividends/annual/${DesTaxYear.fromMtd(taxYear)}")
          .returns(Future.successful(Left(expectedDesResponse)))

        val retrieveDividends: RetrieveDividendsRequest = RetrieveDividendsRequest(Nino(nino), DesTaxYear.fromMtd(taxYear))
        val result: RetrieveDividendsConnectorOutcome  = await(connector.retrieve(retrieveDividends))

        result shouldBe Left(expectedDesResponse)
      }
    }

    "return a response with multiple errors and CorrelationId" when {
      "an request supplied with invalid tax year and invalid Nino " in new Test {

        val expectedDesResponse: DesResponse[MultipleErrors] = DesResponse("X-123", MultipleErrors(Seq(TaxYearFormatError, NinoFormatError)))
        val nino: String = "AA123456A"
        val taxYear: String = "1111-12"

        MockedHttpClient.get[RetrieveDividendsConnectorOutcome](
          s"$baseUrl" + s"/income-tax/nino/$nino/income-source/dividends/annual/${DesTaxYear.fromMtd(taxYear)}")
          .returns(Future.successful(Left(expectedDesResponse)))


        val retrieveDividends: RetrieveDividendsRequest = RetrieveDividendsRequest(Nino(nino), DesTaxYear.fromMtd(taxYear))
        val result: RetrieveDividendsConnectorOutcome = await(connector.retrieve(retrieveDividends))

        result shouldBe Left(expectedDesResponse)
      }
    }


  }
}