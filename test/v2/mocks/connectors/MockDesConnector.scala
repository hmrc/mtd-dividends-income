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

package v2.mocks.connectors

import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import uk.gov.hmrc.http.HeaderCarrier
import v2.connectors.DesConnector
import v2.models.outcomes.{AmendDividendsConnectorOutcome, RetrieveDividendsConnectorOutcome}
import v2.models.requestData.{AmendDividendsRequest, RetrieveDividendsRequest}

import scala.concurrent.{ExecutionContext, Future}

class MockDesConnector extends MockFactory {

  val connector: DesConnector = mock[DesConnector]

  object MockDesConnector {

    def amend(amendDividendsRequest: AmendDividendsRequest): CallHandler[Future[AmendDividendsConnectorOutcome]] = {
      (connector.amend(_: AmendDividendsRequest)(_: HeaderCarrier, _: ExecutionContext))
        .expects(amendDividendsRequest, *, *)
    }


    def retrieve(retrieveDividendsRequest: RetrieveDividendsRequest): CallHandler[Future[RetrieveDividendsConnectorOutcome]] = {
      (connector.retrieve(_: RetrieveDividendsRequest)(_: HeaderCarrier, _: ExecutionContext))
        .expects(retrieveDividendsRequest, *, *)
    }
  }

}
