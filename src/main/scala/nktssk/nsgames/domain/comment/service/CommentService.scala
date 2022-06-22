package nktssk.nsgames.domain.comment.service

import cats.Monad
import cats.syntax.all._
import cats.data.EitherT
import nktssk.nsgames.repositories.comment.CommentRepositoryAlgebra
import nktssk.nsgames.domain.ArticleNotFoundError
import nktssk.nsgames.domain.article.validation.ArticleValidationAlgebra
import nktssk.nsgames.domain.comment.model.Comment
import nktssk.nsgames.domain.users.models.User
import nktssk.nsgames.endpoints.comment.dto.request.CommentRequestModel

import java.util.Date

class CommentService[F[_]](
                            repository: CommentRepositoryAlgebra[F],
                            validation: ArticleValidationAlgebra[F]
                          ) {
  def create(model: CommentRequestModel, user: User, userId: Long)(implicit M: Monad[F]): F[Comment] =
    for {
      comment <- Comment(None, userId, user.firstName + " " + user.lastName, model.articleId, model.text, new Date()).pure[F]
      saved <- repository.create(comment)
    } yield saved

  def get(articleId: Long, pageSize: Int, offset: Int)(implicit M: Monad[F]): EitherT[F, ArticleNotFoundError.type, List[Comment]] = {
    for {
      _ <- validation.exists(articleId.some)
      result <- EitherT.right(repository.list(articleId, pageSize, offset))
    } yield result
  }
}

object CommentService {
  def apply[F[_]](
                   repository: CommentRepositoryAlgebra[F],
                   validation: ArticleValidationAlgebra[F])
  : CommentService[F] =
    new CommentService(repository, validation)
}