
package v2.models.requestData

import uk.gov.hmrc.domain.Nino

case class RetrieveDividendsRequest(nino: Nino, desTaxYear: String)
