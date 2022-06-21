package nktssk.nsgames.endpoints.feedback

import cats.effect.Sync
import cats.syntax.all._
import io.circe.generic.auto._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpRoutes}
import nktssk.nsgames.domain.authentication.Auth
import nktssk.nsgames.domain.feedback.Feedback
import nktssk.nsgames.domain.users.models.User
import nktssk.nsgames.endpoint.AuthEndpoint
import org.http4s.circe.jsonOf
import tsec.authentication.{AugmentedJWT, SecuredRequestHandler, asAuthed}
import tsec.jwt.algorithms.JWTMacAlgo
import nktssk.nsgames.endpoints.feedback.dto.FeedbackRequestModel
import nktssk.nsgames.repositories.feedback.FeedbackRepositoryAlgebra

object FeedbackEndpoints {
  def endpoints[F[_] : Sync, Auth: JWTMacAlgo](
                                                repo: FeedbackRepositoryAlgebra[F],
                                                auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]],
                                              ): HttpRoutes[F] =
    new FeedbackEndpoints[F, Auth].endpoints(repo, auth)
}

class FeedbackEndpoints[F[_] : Sync, Auth: JWTMacAlgo] extends Http4sDsl[F] {

  def endpoints(
                 repo: FeedbackRepositoryAlgebra[F],
                 auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]],
               ): HttpRoutes[F] = {
      auth.liftService(Auth.allRoles {
        createEndpoint(repo)
      })
  }

  private def createEndpoint(repo: FeedbackRepositoryAlgebra[F]): AuthEndpoint[F, Auth] = {
    case req@POST -> Root / "create" asAuthed _ =>
    for {
      model <- req.request.as[FeedbackRequestModel]
      _ <- repo.create(Feedback(None, model.text))
      resp <- Ok()
    } yield resp
  }

  // Implicits
  implicit val feedbackModelDecoder: EntityDecoder[F, FeedbackRequestModel] = jsonOf[F, FeedbackRequestModel]
}