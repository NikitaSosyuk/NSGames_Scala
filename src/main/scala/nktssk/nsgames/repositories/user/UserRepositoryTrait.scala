package nktssk.nsgames.repositories.user

import cats.data.OptionT
import nktssk.nsgames.domain.users.models.User

trait UserRepositoryTrait[F[_]] {
  def create(user: User): F[User]
  def get(id: Long): OptionT[F, User]

  def updateCode(user: User): OptionT[F, User]

  def findByEmail(email: String): OptionT[F, User]
  def findByPhoneNumber(phoneNumber: String): OptionT[F, User]
}
