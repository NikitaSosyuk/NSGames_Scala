package nktssk.nsgames.domain.article.validation

import cats.Applicative
import cats.data.EitherT
import cats.syntax.all._
import cats.implicits.catsSyntaxApplicativeId
import nktssk.nsgames.domain.ArticleNotFoundError
import nktssk.nsgames.repositories.article.ArticleRepositoryAlgebra


object ArticleValidation {
  def apply[F[_] : Applicative](repo: ArticleRepositoryAlgebra[F]): ArticleValidationAlgebra[F] =
    new ArticleValidation[F](repo)
}

class ArticleValidation[F[_] : Applicative](repository: ArticleRepositoryAlgebra[F])
  extends ArticleValidationAlgebra[F] {
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
