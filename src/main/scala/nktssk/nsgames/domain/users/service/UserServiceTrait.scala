package nktssk.nsgames.domain.users.service

import cats.{Functor, Monad}
import cats.data.EitherT
import nktssk.nsgames.domain.{UserAlreadyExistsError, UserNotFoundError}
import nktssk.nsgames.domain.users.models.User

trait UserServiceTrait[F[_]] {
  def create(user: User)(implicit M: Monad[F]): EitherT[F, UserAlreadyExistsError, User]
  def get(id: Long)(implicit F: Functor[F]): EitherT[F, UserNotFoundError.type, User]

  def findByEmail(email: String)(implicit F: Functor[F]): EitherT[F, UserNotFoundError.type , User]
  def findByPhoneNumber(phoneNumber: String)(implicit F: Functor[F]): EitherT[F, UserNotFoundError.type , User]

  def createCode(user: User)(implicit F: Functor[F]): EitherT[F, UserNotFoundError.type, User]
}
