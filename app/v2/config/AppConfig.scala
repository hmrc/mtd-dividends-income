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

package v2.config

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig


trait AppConfig {

  // MTD ID Lookup Config
  def mtdIdBaseUrl: String

  // Downstream Config
  def desBaseUrl: String
  def desEnv: String
  def desToken: String
  def desEnvironmentHeaders: Option[Seq[String]]

  def authServiceValidationEnabled: Boolean
}

@Singleton
class AppConfigImpl @Inject()(config: ServicesConfig, configuration: Configuration) extends AppConfig {

  // MTD ID Lookup Config
  val mtdIdBaseUrl: String = config.baseUrl("mtd-id-lookup")

  // Downstream Config
  val desBaseUrl: String = config.baseUrl("des")
  val desEnv: String = config.getString("microservice.services.des.env")
  val desToken: String = config.getString("microservice.services.des.token")
  val desEnvironmentHeaders: Option[Seq[String]] = configuration.getOptional[Seq[String]]("microservice.services.des.environmentHeaders")

  // API Config
  val authServiceValidationEnabled: Boolean = config.getBoolean(s"api.confidence-level-check.auth-validation.enabled")

}

trait FixedConfig {
  // Minimum tax year for MTD
  val minimumTaxYear = 2018
}
