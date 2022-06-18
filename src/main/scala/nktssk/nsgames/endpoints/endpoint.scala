package nktssk.nsgames

import org.http4s.Response
import nktssk.nsgames.domain.users.models.User
import tsec.authentication.{AugmentedJWT, SecuredRequest, TSecAuthService}

package object endpoint {
  type AuthService[F[_], Auth] = TSecAuthService[User, AugmentedJWT[Auth, Long], F]
  type AuthEndpoint[F[_], Auth] =
    PartialFunction[SecuredRequest[F, User, AugmentedJWT[Auth, Long]], F[Response[F]]]
}
