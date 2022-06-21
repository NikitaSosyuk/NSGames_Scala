package nktssk.nsgames.repositories.article

import cats.data.OptionT
import cats.effect.Bracket
import cats.implicits.catsSyntaxOptionId
import doobie.Transactor
import cats.syntax.all._
import doobie.implicits._
import nktssk.nsgames.domain.article.models.Article
import nktssk.nsgames.repositories.SQLPagination.paginate
import nktssk.nsgames.repositories.article.ArticleSQL._

object DoobieArticleRepository {
  def apply[F[_]: Bracket[*[_], Throwable]](xa: Transactor[F]): DoobieArticleRepository[F] =
    new DoobieArticleRepository(xa)
}

class DoobieArticleRepository[F[_]: Bracket[*[_], Throwable]](val xa: Transactor[F])
  extends ArticleRepositoryAlgebra[F] {

  override def create(article: Article): F[Article] =
    insert(article)
      .withUniqueGeneratedKeys[Long]("id")
      .map(id => article.copy(id = id.some))
      .transact(xa)

  override def get(articleId: Long): OptionT[F, Article] =
    OptionT(select(articleId).option.transact(xa))

  override def list(pageSize: Int, offset: Int): F[List[Article]] =
    paginate(pageSize, offset)(selectAll()).to[List].transact(xa)

  override def delete(articleId: Long): F[Unit] =
    ArticleSQL.delete(articleId).run.as(()).transact(xa)
}