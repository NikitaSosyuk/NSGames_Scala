package nktssk.nsgames.repositories.user

import cats.data.OptionT
import cats.effect.Bracket
import cats.syntax.all._
import doobie._
import doobie.implicits._
import tsec.authentication.IdentityStore
import nktssk.nsgames.domain.users.models.User
import nktssk.nsgames.repositories.user.UserSQL._

object DoobieUserRepository {
  def apply[F[_]: Bracket[*[_], Throwable]](xa: Transactor[F]): DoobieUserRepository[F] =
    new DoobieUserRepository(xa)
}

class DoobieUserRepository[F[_]: Bracket[*[_], Throwable]](val xa: Transactor[F])
  extends UserRepositoryAlgebra[F]
  with IdentityStore[F, Long, User] { self =>

  override def create(user: User): F[User] =
    insert(user)
      .withUniqueGeneratedKeys[Long]("id")
      .map(id => user.copy(id = id.some))
      .transact(xa)

  override def get(id: Long): OptionT[F, User] =
    OptionT(select(id).option.transact(xa))

  override def findByEmail(email: String): OptionT[F, User] =
    OptionT(byEmail(email).option.transact(xa))

  override def findByPhoneNumber(phoneNumber: String): OptionT[F, User] =
    OptionT(byPhoneNumber(phoneNumber).option.transact(xa))

  override def updateCode(confirmCode: String, phoneNumber: String): F[Unit] = {
    UserSQL.update(confirmCode, confirmCode).run.transact(xa).as(())
  }
}
