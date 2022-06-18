package nktssk.nsgames.domain.users.models

import cats.Eq

final case class UserState(name: String)

object UserState {
  val ACTIVE = "ACTIVE"
  var BLOCKED = "BLOCKED"
  var INVALID = "INVALID"

  implicit val eqUserState: Eq[UserState] = Eq.fromUniversalEquals[UserState]
}