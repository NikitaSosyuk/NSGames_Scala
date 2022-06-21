package nktssk.nsgames.domain.comment.model

case class CommentResponse(id: Option[Long], userId: Long, username: String, articleId: Long, text: String, time: String)

object CommentResponse {
  def from(comment: Comment): CommentResponse =
    CommentResponse(comment.id, comment.userId, comment.username, comment.articleId, comment.text, comment.time.toString)
}