package nktssk.nsgames.domain.comment.model

import java.sql.Date

final case class Comment(
                        id: Option[Long],
                        userId: Long,
                        username: String,
                        articleId: Long,
                        text: String,
                        time: Date
                        )
