package nktssk.nsgames.repositories.comment

import nktssk.nsgames.domain.comment.model.Comment

trait CommentRepositoryAlgebra[F[_]] {
  def create(comment: Comment): F[Comment]
  def list(articleId: Long, pageSize: Int, offset: Int): F[List[Comment]]
}
