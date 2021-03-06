package nktssk.nsgames.domain.article.validation

import cats.data.EitherT
import nktssk.nsgames.domain._

trait ArticleValidationAlgebra[F[_]] {
  def exists(id: Option[Long]): EitherT[F, ArticleNotFoundError.type, Unit]
}
