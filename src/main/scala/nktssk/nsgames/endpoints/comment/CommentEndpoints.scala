package nktssk.nsgames.endpoints.comment

import cats.effect.Sync
import cats.syntax.all._
import io.circe.syntax._
import io.circe.generic.auto._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpRoutes}
import nktssk.nsgames.domain.authentication.Auth
import nktssk.nsgames.domain.comment.model.Comment
import nktssk.nsgames.domain.comment.service.CommentService
import nktssk.nsgames.domain.users.models.User
import nktssk.nsgames.endpoint.{AuthEndpoint, AuthService}
import nktssk.nsgames.endpoints.Pagination.{OptionalOffsetMatcher, OptionalPageSizeMatcher}

import java.util.Date
import org.http4s.circe.jsonOf
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
        createArticleEndpoint(commentService)
          .orElse(listForArticle(commentService))
      }
    }
    auth.liftService(authEndpoints)
  }

  private def createArticleEndpoint(commentService: CommentService[F]): AuthEndpoint[F, Auth] = {
    case req@POST -> Root / "create" asAuthed user =>
      user.id match {
        case Some(id) =>
          for {
            model <- req.request.as[CommentRequestModel]
            comment <- Comment(None, id, s"$user.firstName  $user.lastName", model.articleId, model.text, new Date()).pure[F]
            result <- commentService.create(comment)
            resp <- Ok(result.asJson)
          } yield resp
        case None =>
          NotFound("user id not found")
      }
  }

  private def listForArticle(commentService: CommentService[F]): AuthEndpoint[F, Auth] = {
    case GET -> Root / "list" / LongVar(id) :? OptionalPageSizeMatcher(pageSize) :? OptionalOffsetMatcher(offset) asAuthed _ =>
      for {
        result <- commentService.get(id, pageSize.getOrElse(40), offset.getOrElse(0)).pure[F]
        resp <- Ok(result.asJson)
      } yield resp
  }

  // Implicits
  implicit val commentDecoder: EntityDecoder[F, Comment] = jsonOf
  implicit val commentModelDecoder: EntityDecoder[F, CommentRequestModel] = jsonOf[F, CommentRequestModel]
}