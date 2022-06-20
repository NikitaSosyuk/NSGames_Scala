package nktssk.nsgames.domain.article.validation

import cats.Applicative
import cats.data.EitherT
import cats.syntax.all._
import cats.implicits.catsSyntaxApplicativeId
import nktssk.nsgames.domain.ArticleNotFoundError
import nktssk.nsgames.repositories.article.ArticleRepositoryTrait


object ArticleValidation {
  def apply[F[_] : Applicative](repo: ArticleRepositoryTrait[F]): ArticleValidationTrait[F] =
    new ArticleValidation[F](repo)
}

class ArticleValidation[F[_] : Applicative](repository: ArticleRepositoryTrait[F])
  extends ArticleValidationTrait[F] {
  override def exists(id: Option[Long]): EitherT[F, ArticleNotFoundError.type, Unit] =
    id match {
      case Some(id) =>
        repository.get(id)
          .toRight(ArticleNotFoundError)
          .void
      case None =>
        EitherT.left[Unit](ArticleNotFoundError.pure[F])
    }
}
