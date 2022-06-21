package nktssk.nsgames.repositories.feedback

import cats.effect.Bracket
import cats.implicits.{catsSyntaxOptionId, toFunctorOps}
import doobie.Transactor
import doobie.implicits._
import FeedbackSQL._
import nktssk.nsgames.domain.feedback.Feedback

object DoobieFeedbackRepository {
  def apply[F[_]: Bracket[*[_], Throwable]](xa: Transactor[F]): DoobieFeedbackRepository[F] =
    new DoobieFeedbackRepository(xa)
}

class DoobieFeedbackRepository[F[_]: Bracket[*[_], Throwable]](val xa: Transactor[F])
  extends FeedbackRepositoryAlgebra[F] {

  override def create(feedback: Feedback): F[Unit] =
    insert(feedback)
      .withUniqueGeneratedKeys[Long]("id")
      .map(id => feedback.copy(id = id.some))
      .as(())
      .transact(xa)
}