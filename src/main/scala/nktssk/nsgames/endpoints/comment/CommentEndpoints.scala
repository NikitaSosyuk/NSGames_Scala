package nktssk.nsgames.endpoints.comment

import cats.effect.Sync
import cats.syntax.all._
import io.circe.syntax._
import io.circe.generic.auto._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpRoutes}
import nktssk.nsgames.domain.authentication.Auth
import nktssk.nsgames.domain.comment.model.{Comment, CommentResponse}
import nktssk.nsgames.domain.comment.service.CommentService
import nktssk.nsgames.domain.users.models.User
import nktssk.nsgames.endpoint.{AuthEndpoint, AuthService}
import nktssk.nsgames.endpoints.Pagination.{OptionalOffsetMatcher, OptionalPageSizeMatcher}

import java.util.Date
import org.http4s.circe.{jsonEncoder, jsonOf}
import tsec.authentication.{AugmentedJWT, SecuredRequestHandler, asAuthed}
import tsec.jwt.algorithms.JWTMacAlgo
import nktssk.nsgames.endpoints.comment.dto.CommentRequestModel

object CommentEndpoints {
  def endpoints[F[_] : Sync, Auth: JWTMacAlgo](
                                                commentService: CommentService[F],
                                                auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]],
                                              ): HttpRoutes[F] =
    new CommentEndpoints[F, Auth].endpoints(commentService, auth)
}

class CommentEndpoints[F[_] : Sync, Auth: JWTMacAlgo] extends Http4sDsl[F] {

  def endpoints(
                 commentService: CommentService[F],
                 auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]],
               ): HttpRoutes[F] = {
    val authEndpoints: AuthService[F, Auth] = {
      Auth.allRoles {
        createCommentEndpoint(commentService)
          .orElse(listForComment(commentService))
      }
    }
    auth.liftService(authEndpoints)
  }

  private def createCommentEndpoint(commentService: CommentService[F]): AuthEndpoint[F, Auth] = {
    case req@POST -> Root / "create" asAuthed user =>
      user.id match {
        case Some(id) =>
          for {
            model <- req.request.as[CommentRequestModel]
            result <- commentService.create(Comment(None, id, user.firstName + " " + user.lastName, model.articleId, model.text, new Date()))
            resp <- Ok(CommentResponse.from(result).asJson)
          } yield resp
        case None =>
          NotFound("User id not found")
      }
  }

  private def listForComment(commentService: CommentService[F]): AuthEndpoint[F, Auth] = {
    case GET -> Root / "list" / LongVar(id) :? OptionalPageSizeMatcher(pageSize) :? OptionalOffsetMatcher(offset) asAuthed _ =>
      val action = for {
        action <- commentService.get(id, pageSize.getOrElse(40), offset.getOrElse(0)).value
      } yield action

      action.flatMap {
        case Right(value) => Ok(value.map{CommentResponse.from}.asJson)
        case Left(_) => BadRequest("Incorrect article id")
      }
  }

  // Implicits
  implicit val commentDecoder: EntityDecoder[F, CommentResponse] = jsonOf
  implicit val commentModelDecoder: EntityDecoder[F, CommentRequestModel] = jsonOf[F, CommentRequestModel]
}