package nktssk.nsgames.repositories.article

import doobie.implicits.toSqlInterpolator
import doobie.{Query0, Update0}
import nktssk.nsgames.domain.article.models.Article

object ArticleSQL {
  def insert(article: Article): Update0 = sql"""
    INSERT INTO ARTICLE (USER_ID, HEADER, BODY, VISIBLE)
    VALUES (${article.userId} ${article.header}, ${article.body}, ${article.isVisible})
  """.update

  def select(id: Long): Query0[Article] = sql"""
    SELECT ID, USER_ID, VISIBLE, HEADER, BODY
    FROM ARTICLE
    WHERE ID = $id
  """.query[Article]

  def selectAll(): Query0[Article] = sql"""
    SELECT ID, USER_ID, VISIBLE, HEADER, BODY
    FROM ARTICLE
  """.query
}
