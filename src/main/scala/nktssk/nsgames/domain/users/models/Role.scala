package nktssk.nsgames.domain.users.models

import cats.Eq
import tsec.authorization.{AuthGroup, SimpleAuthEnum}

final case class Role(name: String)

object Role extends SimpleAuthEnum[Role, String] {
  val Admin: Role = Role("Admin")
  val User: Role = Role("User")
  override val values: AuthGroup[Role] = AuthGroup(Admin, User)
  implicit val eqRole: Eq[Role] = Eq.fromUniversalEquals[Role]

  override def getRepr(t: Role): String = t.name
}
