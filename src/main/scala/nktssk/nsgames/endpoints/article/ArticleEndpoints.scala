package nktssk.nsgames.endpoints.article

import cats.effect.Sync
import io.circe.generic.auto._
import io.circe.syntax._
import cats.syntax.all._
import nktssk.nsgames.domain.article.models.Article
import nktssk.nsgames.domain.article.service.ArticleService
import nktssk.nsgames.domain.authentication.Auth
import nktssk.nsgames.domain.users.models.User
import nktssk.nsgames.endpoint.{AuthEndpoint, AuthService}
import nktssk.nsgames.endpoints.Pagination.OptionalPageSizeMatcher
import nktssk.nsgames.endpoints.article.dto.ArticleRequestModel
import org.http4s.circe.jsonOf
import org.http4s.{EntityDecoder, HttpRoutes}
import org.http4s.dsl.Http4sDsl
import tsec.authentication.{AugmentedJWT, SecuredRequestHandler, asAuthed}
import tsec.jwt.algorithms.JWTMacAlgo
import org.http4s.circe._
import nktssk.nsgames.endpoints.Pagination._

object ArticleEndpoints {
  def endpoints[F[_] : Sync, Auth: JWTMacAlgo](
                                                articleService: ArticleService[F],
                                                auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]],
                                              ): HttpRoutes[F] =
    new ArticleEndpoints[F, Auth].endpoints(articleService, auth)
}


class ArticleEndpoints[F[_] : Sync, Auth: JWTMacAlgo] extends Http4sDsl[F] {

  def endpoints(
                 articleService: ArticleService[F],
                 auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]],
               ): HttpRoutes[F] = {
    val authEndpoints: AuthService[F, Auth] = {
      Auth.allRoles {
        createArticleEndpoint(articleService)
          .orElse(getArticleEndpoint(articleService))
          .orElse(getListArticleEndpoint(articleService))
          .orElse(getHeaderListArticleEndpoint(articleService))
      }
    }
    auth.liftService(authEndpoints)
  }

  private def createArticleEndpoint(articleService: ArticleService[F]): AuthEndpoint[F, Auth] = {
    case req@POST -> Root / "create" asAuthed user =>
      user.id match {
        case Some(id) =>
          for {
            model <- req.request.as[ArticleRequestModel]
            article <- Article(None, id, isVisible = true, model.header, model.body).pure[F]
            result <- articleService.create(article)
            resp <- Ok(result.asJson)
          } yield resp
        case None =>
          NotFound("user id not found")
      }
  }

  private def getArticleEndpoint(articleService: ArticleService[F]): AuthEndpoint[F, Auth] = {
    case GET -> Root / LongVar(id) asAuthed _ =>
      for {
        result <- articleService.get(id).value
        resp <- Ok(result.asJson)
      } yield resp
  }

  private def getListArticleEndpoint(articleService: ArticleService[F]): AuthEndpoint[F, Auth] = {
    case GET -> Root / "list" :? OptionalPageSizeMatcher(pageSize) :? OptionalOffsetMatcher(offset) asAuthed _ =>
      for {
        result <- articleService.list(pageSize.getOrElse(40), offset.getOrElse(0))
        resp <- Ok(result.asJson)
      } yield resp
  }

  private def getHeaderListArticleEndpoint(articleService: ArticleService[F]): AuthEndpoint[F, Auth] = {
    case GET -> Root / "headers" :? OptionalPageSizeMatcher(pageSize) :? OptionalOffsetMatcher(offset) asAuthed _ =>
      for {
        result <- articleService.list(pageSize.getOrElse(40), offset.getOrElse(0))
        resp <- Ok(result.map {_.header}.asJson)
      } yield resp
  }

  // Implicits
  implicit val articleDecoder: EntityDecoder[F, Article] = jsonOf[F, Article]
  implicit val articleModelDecoder: EntityDecoder[F, ArticleRequestModel] = jsonOf[F, ArticleRequestModel]
}