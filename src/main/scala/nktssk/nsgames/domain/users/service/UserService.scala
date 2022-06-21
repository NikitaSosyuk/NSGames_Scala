package nktssk.nsgames.domain.users.service

import cats.Monad
import cats.data.EitherT
import nktssk.nsgames.domain.users.models.User
import nktssk.nsgames.domain.users.validation.UserValidationAlgebra
import nktssk.nsgames.repositories.user.UserRepositoryAlgebra
import cats.Functor
import nktssk.nsgames.domain.{UserAlreadyExistsError, UserNotFoundError}

object UserService {
  def apply[F[_]](
                   repository: UserRepositoryAlgebra[F],
                   validation: UserValidationAlgebra[F],
                 ): UserService[F] =
    new UserService[F](repository, validation)
}

class UserService[F[_]](
                         userRepo: UserRepositoryAlgebra[F],
                         validation: UserValidationAlgebra[F]
                       ) extends UserServiceAlgebra[F] {
  override def create(user: User)(implicit M: Monad[F]): EitherT[F, UserAlreadyExistsError, User] = {
    for {
      _ <- validation.doesNotExist(user)
      saved <- EitherT.liftF(userRepo.create(user))
    } yield saved
  }

  override def get(id: Long)(implicit F: Functor[F]): EitherT[F, UserNotFoundError.type, User] =
    userRepo.get(id).toRight(UserNotFoundError)

  override def findByEmail(email: String)(implicit F: Functor[F]): EitherT[F, UserNotFoundError.type , User] =
    userRepo.findByEmail(email).toRight(UserNotFoundError)

  override def findByPhoneNumber(phoneNumber: String)(implicit F: Functor[F]): EitherT[F, UserNotFoundError.type , User] =
    userRepo.findByPhoneNumber(phoneNumber).toRight(UserNotFoundError)
}
