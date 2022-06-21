package nktssk.nsgames.repositories.comment

import doobie.implicits.toSqlInterpolator
import doobie.{Query0, Update0}
import nktssk.nsgames.domain.comment.model.Comment

object CommentSQL {
  def insert(comment: Comment): Update0 = sql"""
      INSERT INTO COMMENT (USER_ID, ARTICLE_ID, USERNAME, TEXT, TIME)
    VALUES (${comment.userId}, ${comment.articleId}, ${comment.username}, ${comment.text}, ${comment.time})
  """.update

  def select(articleId: Long): Query0[Comment] = sql"""
    SELECT ID, USER_ID, USERNAME, ARTICLE_ID, TEXT, TIME
    FROM COMMENT
    WHERE ARTICLE_ID = $articleId
  """.query
}
