package nktssk.nsgames.repositories.article

import cats.data.OptionT
import nktssk.nsgames.domain.article.models.Article

trait ArticleRepositoryAlgebra[F[_]] {
  def create(article: Article): F[Article]
  def get(articleId: Long): OptionT[F, Article]
  def delete(articleId: Long): F[Unit]
  def list(pageSize: Int, offset: Int): F[List[Article]]
}
