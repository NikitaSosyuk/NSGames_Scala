package nktssk.nsgames

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

package object config {
  implicit val srDec: Decoder[ServerConfig] = deriveDecoder
  implicit val dbconnDec: Decoder[DBConnectionConfig] = deriveDecoder
  implicit val dbDec: Decoder[DBConfig] = deriveDecoder
  implicit val psDec: Decoder[ApplicationConfig] = deriveDecoder
}

