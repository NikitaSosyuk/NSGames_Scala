package nktssk.nsgames.domain.users.validation

import cats.data.EitherT
import nktssk.nsgames.domain.UserNotFoundError
import nktssk.nsgames.domain.users.models.User
import nktssk.nsgames.domain.UserAlreadyExistsError

trait UserValidationTrait[F[_]] {
  def doesNotExist(user: User): EitherT[F, UserAlreadyExistsError, Unit]
  def exists(userId: Option[Long]): EitherT[F, UserNotFoundError.type, Unit]
}