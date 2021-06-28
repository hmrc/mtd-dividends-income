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

package v2.controllers.requestParsers

import javax.inject.Inject
import v2.controllers.requestParsers.validators.AmendDividendsValidator
import v2.models.Dividends
import v2.models.domain.Nino
import v2.models.errors.{BadRequestError, ErrorWrapper}
import v2.models.requestData.{AmendDividendsRequest, AmendDividendsRequestRawData, DesTaxYear}
import v2.utils.Logging

class AmendDividendsRequestDataParser @Inject()(validator: AmendDividendsValidator) extends Logging {

  def parse(data: AmendDividendsRequestRawData)(implicit correlationId: String): Either[ErrorWrapper, AmendDividendsRequest] = {
    validator.validate(data) match {
      case Nil =>
        logger.info(
          "[RequestParser][parseRequest] " +
            s"Validation successful for the request with CorrelationId: $correlationId")
        Right(AmendDividendsRequest(Nino(data.nino), DesTaxYear.fromMtd(data.taxYear), data.body.json.as[Dividends]))
      case err :: Nil =>
        logger.info(
          "[RequestParser][parseRequest] " +
            s"Validation failed with ${err.code} error for the request with CorrelationId: $correlationId")
        Left(ErrorWrapper(correlationId, err, None))
      case errs =>
        logger.info(
          "[RequestParser][parseRequest] " +
            s"Validation failed with ${errs.map(_.code).mkString(",")} error for the request with CorrelationId: $correlationId")
        Left(ErrorWrapper(correlationId, BadRequestError, Some(errs)))
    }
  }

}