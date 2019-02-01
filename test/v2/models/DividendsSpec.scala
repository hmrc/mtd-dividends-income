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

package v2.models

import play.api.libs.json.JsValue
import support.UnitSpec
import v2.fixtures.Fixtures.DividendsFixture._
import v2.models.requestData.DesTaxYear



class DividendsSpec extends UnitSpec {

  val taxYear = "2017-18"


  "reads" should {

    "return a Dividends model" when {
      "correct Json is supplied" in {
        val model = Dividends.reads.reads(mtdFormatJson).get
        model shouldBe dividendsModel
      }
    }

  }

  "writes" should {

    "generate a valid JSON" when {
      "a valid model is retrieved" in {
        val json: JsValue = Dividends.writes.writes(dividendsModel)
        json shouldBe mtdFormatJson
      }
    }
  }

  "DesTaxYear" should {
    "generate tax year" when {
      "given a year" in {
        val year = DesTaxYear(taxYear).toDesTaxYear
        year shouldBe "2018"
      }
    }
  }

}
