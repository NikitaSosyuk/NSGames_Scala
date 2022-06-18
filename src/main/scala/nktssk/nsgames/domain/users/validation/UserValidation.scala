package nktssk.nsgames.domain.users.validation

import cats.Applicative
import cats.data.EitherT
import cats.syntax.all._
import nktssk.nsgames.domain.{UserAlreadyExistsError, UserNotFoundError}
import nktssk.nsgames.domain.users.models.User
import nktssk.nsgames.repositories.user.UserRepositoryTrait

class UserValidation[F[_] : Applicative](userRepo: UserRepositoryTrait[F])
  extends UserValidationTrait[F] {
  def doesNotExist(user: User): EitherT[F, UserAlreadyExistsError, Unit] =
    userRepo
      .findByPhoneNumber(user.phoneNumber)
      .map(user => UserAlreadyExistsError(phoneNumber = user.phoneNumber))
      .toLeft(())

  def exists(userId: Option[Long]): EitherT[F, UserNotFoundError.type, Unit] =
    userId match {
      case Some(id) =>
        userRepo
          .get(id)
          .toRight(UserNotFoundError)
          .void
      case None =>
        EitherT.left[Unit](UserNotFoundError.pure[F])
    }
}

object UserValidation {
  def apply[F[_] : Applicative](repo: UserRepositoryTrait[F]): UserValidationTrait[F] =
    new UserValidation[F](repo)
}