package nktssk.nsgames.domain.article.models

final case class Article(
                          id: Option[Long] = None,
                          userId: Long,
                          isVisible: Boolean = true,
                          header: String,
                          body: String
                        )
