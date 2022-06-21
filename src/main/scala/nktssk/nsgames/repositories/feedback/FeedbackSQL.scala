package nktssk.nsgames.repositories.feedback

import doobie.implicits.toSqlInterpolator
import doobie.Update0
import nktssk.nsgames.domain.feedback.Feedback

object FeedbackSQL {
  def insert(comment: Feedback): Update0 = sql"""
    INSERT INTO FEEDBACK (TEXT)
    VALUES (${comment.text})
  """.update
}