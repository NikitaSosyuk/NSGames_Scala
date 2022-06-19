package nktssk.nsgames.repositories.article

import nktssk.nsgames.domain.article.models.Article

trait ArticleRepositoryTrait[F[_]] {
  def create(article: Article): F[Article]
  def get(articleId: Long): F[Option[Article]]
  def list(pageSize: Int, offset: Int): F[List[Article]]
}
