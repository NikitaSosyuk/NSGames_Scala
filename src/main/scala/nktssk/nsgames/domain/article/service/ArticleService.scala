package nktssk.nsgames.domain.article.service

import cats.Monad
import cats.syntax.all._
import cats.data.EitherT
import nktssk.nsgames.domain.ArticleNotFoundError
import nktssk.nsgames.domain.article.models.Article
import nktssk.nsgames.domain.article.validation.ArticleValidationTrait
import nktssk.nsgames.repositories.article.ArticleRepositoryTrait

class ArticleService[F[_]](
                            repository: ArticleRepositoryTrait[F],
                            validation: ArticleValidationTrait[F]
                          ) {
  def create(article: Article)(implicit M: Monad[F]): F[Article] =
    for {
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
                   repository: ArticleRepositoryTrait[F],
                   validation: ArticleValidationTrait[F])
  : ArticleService[F] =
    new ArticleService(repository, validation)
}
