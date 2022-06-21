package nktssk.nsgames.endpoints.user

import cats.data.EitherT
import cats.effect.Sync
import cats.syntax.all._
import io.circe.generic.auto._
import io.circe.syntax._
import nktssk.nsgames.domain.{UserAlreadyExistsError, UserAuthenticationFailedError}
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpRoutes}
import tsec.jwt.algorithms.JWTMacAlgo
import tsec.passwordhashers.{PasswordHash, PasswordHasher}
import tsec.authentication._
import nktssk.nsgames.domain.users.models.{Role, User, UserState}
import nktssk.nsgames.domain.users.service.UserService
import nktssk.nsgames.endpoints.user.dto.request.{LoginRequestModel, SignupRequestModel}
import nktssk.nsgames.endpoints.user.dto.response.UserResponse
import tsec.common.Verified

object UserEndpoints {
  def endpoints[F[_] : Sync, A, Auth: JWTMacAlgo](
                                                   userService: UserService[F],
                                                   cryptService: PasswordHasher[F, A],
                                                   auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]]
                                                 ): HttpRoutes[F] = {
    new UserEndpoints[F, A, Auth].endpoints(userService, cryptService, auth)
  }
}

class UserEndpoints[F[_] : Sync, A, Auth: JWTMacAlgo] extends Http4sDsl[F] {
  def endpoints(
                 userService: UserService[F],
                 cryptService: PasswordHasher[F, A],
                 auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]],
               ): HttpRoutes[F] = {
    loginEndpoint(userService, cryptService, auth.authenticator) <+>
      signupEndpoint(userService, cryptService)
  }

  private def signupEndpoint(
                              userService: UserService[F],
                              crypt: PasswordHasher[F, A],
                            ): HttpRoutes[F] =
    HttpRoutes.of[F] { case req@POST -> Root / "signup" =>
      val action = for {
        signup <- req.as[SignupRequestModel]
        hash <- crypt.hashpw(signup.password)
        user <- User(None, signup.firstname, signup.lastname, signup.phoneNumber, None, hash, signup.email, Role.User, UserState.Active).pure[F]
        result <- userService.create(user).value
      } yield result

      action.flatMap {
        case Right(saved) => Ok(UserResponse.from(saved).asJson)
        case Left(UserAlreadyExistsError(phoneNumber)) =>
          Conflict(s"The user with [phone = ${phoneNumber}] already exists")
      }
    }

  private def loginEndpoint(
                             userService: UserService[F],
                             cryptService: PasswordHasher[F, A],
                             auth: Authenticator[F, Long, User, AugmentedJWT[Auth, Long]],
                           ): HttpRoutes[F] =
    HttpRoutes.of[F] { case req@POST -> Root / "login" =>
      val action = for {
        login <- EitherT.liftF(req.as[LoginRequestModel])
        phone = login.phone
        user <- userService.findByPhoneNumber(phone).leftMap(_ => UserAuthenticationFailedError(phoneNumber = phone))
        checkResult <- EitherT.liftF(
          cryptService.checkpw(login.password, PasswordHash[A](user.password)),
        )
        _ <-
          if (checkResult == Verified) EitherT.rightT[F, UserAuthenticationFailedError](())
          else EitherT.leftT[F, User](UserAuthenticationFailedError(phone))
        token <- user.id match {
          case None => throw new Exception("Impossible")
          case Some(id) => EitherT.right[UserAuthenticationFailedError](auth.create(id))
        }
      } yield (user, token)

      action.value.flatMap {
        case Right((user, token)) => Ok(UserResponse.from(user).asJson).map(auth.embed(_, token))
        case Left(UserAuthenticationFailedError(name)) =>
          BadRequest(s"Authentication failed for user $name")
      }
    }

  // Implicits
  implicit val userDecoder: EntityDecoder[F, User] = jsonOf
  implicit val loginReqDecoder: EntityDecoder[F, LoginRequestModel] = jsonOf
  implicit val signupReqDecoder: EntityDecoder[F, SignupRequestModel] = jsonOf
}
