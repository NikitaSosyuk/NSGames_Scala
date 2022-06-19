package nktssk.nsgames.domain.users.models

import cats.Applicative
import tsec.authorization.AuthorizationInfo

case class User(
                 id: Option[Long] = None,
                 firstName: String,
                 lastName: String,
                 phoneNumber: String,
                 confirmCode: Option[String] = None,
                 password: String,
                 email: String,
                 role: Role,
                 state: UserState
               )

object User {
  implicit def authRole[F[_]](implicit F: Applicative[F]): AuthorizationInfo[F, Role, User] =
    new AuthorizationInfo[F, Role, User] {
      def fetchInfo(u: User): F[Role] = F.pure(u.role)
    }
}