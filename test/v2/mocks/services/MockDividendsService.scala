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

package v2.mocks.services

import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import uk.gov.hmrc.http.HeaderCarrier
import v2.models.outcomes.{AmendDividendsOutcome, RetrieveDividendsOutcome}
import v2.models.requestData.{AmendDividendsRequest, RetrieveDividendsRequest}
import v2.services.DividendsService

import scala.concurrent.{ExecutionContext, Future}

trait MockDividendsService extends MockFactory{

  val mockDividendsService: DividendsService = mock[DividendsService]

  object MockDividendsService {
    def amend(DividendsRequest: AmendDividendsRequest): CallHandler[Future[AmendDividendsOutcome]] = {
      (mockDividendsService.amend(_:AmendDividendsRequest)(_: HeaderCarrier, _: ExecutionContext, _:String))
        .expects(DividendsRequest, *, *, *)
    }

    def retrieve(retrieveDividendsRequest: RetrieveDividendsRequest): CallHandler[Future[RetrieveDividendsOutcome]] = {
      (mockDividendsService.retrieve(_: RetrieveDividendsRequest)(_: HeaderCarrier, _: ExecutionContext, _:String))
        .expects(retrieveDividendsRequest, *, *, *)
    }
  }

}