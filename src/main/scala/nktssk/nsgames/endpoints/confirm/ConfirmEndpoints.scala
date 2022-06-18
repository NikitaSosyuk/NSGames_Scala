package nktssk.nsgames.endpoints.confirm

import cats.effect.Sync
import cats.syntax.all._
import nktssk.nsgames.domain.authentication.Auth
import org.http4s.dsl.Http4sDsl
import org.http4s.HttpRoutes
import tsec.jwt.algorithms.JWTMacAlgo
import tsec.authentication._
import nktssk.nsgames.domain.users.models.User
import nktssk.nsgames.domain.users.service.UserService
import nktssk.nsgames.endpoint.AuthEndpoint

object ConfirmEndpoints {
  def endpoints[F[_] : Sync, Auth: JWTMacAlgo](
                                                   userService: UserService[F],
                                                   auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]]
                                                 ): HttpRoutes[F] = {
    new ConfirmEndpoints[F, Auth].endpoints(userService, auth)
  }
}

class ConfirmEndpoints[F[_] : Sync, Auth: JWTMacAlgo] extends Http4sDsl[F] {
  def endpoints(
                 userService: UserService[F],
                 auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]],
               ): HttpRoutes[F] = {
    auth.liftService(Auth.allRoles {
      updateDataEndpoint(userService)
    })
  }

  private def updateDataEndpoint(userService: UserService[F]): AuthEndpoint[F, Auth] = {
    case POST -> Root asAuthed user =>
      val result = for {
        result <- userService.createCode(user).value
      } yield result

      result.flatMap {
        case Right(_) => Ok("done")
        case Left(_) =>
          Conflict("Failed create code because user does not exist")
      }
  }
}
