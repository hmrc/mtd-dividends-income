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

package v2.fixtures

import play.api.libs.json.{JsValue, Json}
import v2.models.Dividends

object Fixtures {

  object DividendsFixture {
    val mtdFormatJson: JsValue = Json.parse(
      s"""{
         |  "ukDividends": 500.25,
         |  "otherUkDividends": 100.25
         |}""".stripMargin

    )

    val dividendsModel: Dividends = Dividends(
      ukDividends = Some(500.25),
      otherUkDividends = Some(100.25))


  }

}
