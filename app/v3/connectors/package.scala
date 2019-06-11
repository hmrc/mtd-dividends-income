package v3

import v2.models.errors.DesError
import v2.models.outcomes.DesResponse

package object connectors {

  type DesConnectorOutcome[A] = Either[DesResponse[DesError], DesResponse[A]]

}
