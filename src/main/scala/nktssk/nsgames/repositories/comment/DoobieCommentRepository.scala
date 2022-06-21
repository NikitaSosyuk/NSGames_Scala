package nktssk.nsgames.repositories.comment

import cats.effect.Bracket
import cats.implicits.catsSyntaxOptionId
import doobie.Transactor
import doobie.implicits._
import nktssk.nsgames.domain.comment.model.Comment
import nktssk.nsgames.repositories.SQLPagination.paginate
import CommentSQL._

object DoobieCommentRepository {
  def apply[F[_]: Bracket[*[_], Throwable]](xa: Transactor[F]): DoobieCommentRepository[F] =
    new DoobieCommentRepository(xa)
}

class DoobieCommentRepository[F[_]: Bracket[*[_], Throwable]](val xa: Transactor[F])
  extends CommentRepositoryAlgebra[F] {

  override def create(comment: Comment): F[Comment] =
    insert(comment)
      .withUniqueGeneratedKeys[Long]("id")
      .map(id => comment.copy(id = id.some))
      .transact(xa)

  def list(articleId: Long, pageSize: Int, offset: Int): F[List[Comment]] =
    paginate(pageSize, offset)(select(articleId)).to[List].transact(xa)
}