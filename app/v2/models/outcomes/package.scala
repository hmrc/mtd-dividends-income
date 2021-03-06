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

package v2.models

import v2.models.auth.UserDetails
import v2.models.errors.{DesError, ErrorWrapper, MtdError}

package object outcomes {

  type AuthOutcome = Either[MtdError, UserDetails]
  type MtdIdLookupOutcome = Either[MtdError, String]

  type AmendDividendsConnectorOutcome = Either[DesResponse[DesError], DesResponse[String]]
  type AmendDividendsOutcome = Either[ErrorWrapper, DesResponse[String]]


  type RetrieveDividendsConnectorOutcome = Either[DesResponse[DesError], DesResponse[Dividends]]
  type RetrieveDividendsOutcome = Either[ErrorWrapper, DesResponse[Dividends]]
}
