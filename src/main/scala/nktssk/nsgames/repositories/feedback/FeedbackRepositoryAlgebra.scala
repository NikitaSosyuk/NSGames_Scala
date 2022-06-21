package nktssk.nsgames.repositories.feedback

import nktssk.nsgames.domain.feedback.Feedback

trait FeedbackRepositoryAlgebra[F[_]] {
  def create(feedback: Feedback): F[Unit]
}
