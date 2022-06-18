package nktssk.nsgames.domain.users.service

import cats.Monad
import cats.data.EitherT
import nktssk.nsgames.domain._
import nktssk.nsgames.domain.users.models.User
import nktssk.nsgames.domain.users.validation.UserValidationTrait
import nktssk.nsgames.repositories.user.UserRepositoryTrait
import cats.Functor

import scala.util.Random

object UserService {
  def apply[F[_]](
                   repository: UserRepositoryTrait[F],
                   validation: UserValidationTrait[F],
                 ): UserService[F] =
    new UserService[F](repository, validation)
}

class UserService[F[_]](userRepo: UserRepositoryTrait[F], validation: UserValidationTrait[F]) extends UserServiceTrait[F] {

  private val random = new Random()

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

  override def createCode(user: User)(implicit F: Functor[F]): EitherT[F, UserNotFoundError.type, User] = {
    val code = random.nextInt().toString
    val updatedUser = user.copy(confirmCode = Some(code))
    userRepo.updateCode(updatedUser).toRight(UserNotFoundError)
  }
}
