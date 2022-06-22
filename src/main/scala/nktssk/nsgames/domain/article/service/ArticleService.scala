package nktssk.nsgames.domain.article.service

import cats.Monad
import cats.syntax.all._
import cats.data.EitherT
import nktssk.nsgames.domain.ArticleNotFoundError
import nktssk.nsgames.domain.article.models.Article
import nktssk.nsgames.domain.article.validation.ArticleValidationAlgebra
import nktssk.nsgames.endpoints.article.dto.ArticleRequestModel
import nktssk.nsgames.repositories.article.ArticleRepositoryAlgebra

class ArticleService[F[_]](
                            repository: ArticleRepositoryAlgebra[F],
                            validation: ArticleValidationAlgebra[F]
                          ) {
  def create(model: ArticleRequestModel, userId: Long)(implicit M: Monad[F]): F[Article] =
    for {
      article <- Article(None, userId, isVisible = true, model.header, model.body).pure[F]
      saved <- repository.create(article)
    } yield saved

  def get(id: Long)(implicit M: Monad[F]): EitherT[F, ArticleNotFoundError.type, Article] = {
    for {
          _ <- validation.exists(Some(id))
         result <-repository.get(id).toRight(ArticleNotFoundError)
      } yield result
  }

  def list(pageSize: Int, offset: Int): F[List[Article]] =
    repository.list(pageSize, offset)

  def delete(id: Long)(implicit M: Monad[F]): EitherT[F, ArticleNotFoundError.type, Unit] = {
    for {
      _ <- validation.exists(Some(id))
      _ <- EitherT.right[ArticleNotFoundError.type](repository.delete(id))
    } yield ()
  }
}

object ArticleService {
  def apply[F[_]](
                   repository: ArticleRepositoryAlgebra[F],
                   validation: ArticleValidationAlgebra[F])
  : ArticleService[F] =
    new ArticleService(repository, validation)
}
