package nktssk.nsgames

import config._
import cats.effect._
import org.http4s.server.{Router, Server => H4Server}
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.implicits._
import tsec.passwordhashers.jca.BCrypt
import doobie.util.ExecutionContexts
import io.circe.config.parser
import nktssk.nsgames.domain.authentication.Auth
import nktssk.nsgames.domain.users.service.UserService
import nktssk.nsgames.domain.comment.service.CommentService
import nktssk.nsgames.domain.article.validation.ArticleValidation
import nktssk.nsgames.domain.article.service.ArticleService
import nktssk.nsgames.domain.users.validation.UserValidation
import nktssk.nsgames.endpoints.article.ArticleEndpoints
import nktssk.nsgames.endpoints.comment.CommentEndpoints
import nktssk.nsgames.endpoints.feedback.FeedbackEndpoints
import nktssk.nsgames.endpoints.user.UserEndpoints
import nktssk.nsgames.repositories.article.DoobieArticleRepository
import nktssk.nsgames.repositories.comment.DoobieCommentRepository
import nktssk.nsgames.repositories.auth.DoobieAuthRepositoryInterpreter
import nktssk.nsgames.repositories.feedback.DoobieFeedbackRepository
import nktssk.nsgames.repositories.user.DoobieUserRepository
import tsec.authentication.SecuredRequestHandler
import tsec.mac.jca.HMACSHA256


object Server extends IOApp {
  def createServer[F[_]: ContextShift: ConcurrentEffect: Timer]: Resource[F, H4Server[F]] =
    for {
      // Configuration
      conf <- Resource.eval(parser.decodePathF[F, ApplicationConfig]("nsgames"))
      serverEc <- ExecutionContexts.cachedThreadPool[F]
      connEc <- ExecutionContexts.fixedThreadPool[F](conf.db.connections.poolSize)
      txnEc <- ExecutionContexts.cachedThreadPool[F]
      xa <- DatabaseConfig.dbTransactor(conf.db, connEc, Blocker.liftExecutionContext(txnEc))
      key <- Resource.eval(HMACSHA256.buildKey[F]("NS is the best".getBytes()))

      // Repositories
      authRepo = DoobieAuthRepositoryInterpreter[F, HMACSHA256](key, xa)
      userRepo = DoobieUserRepository[F](xa)
      articleRepo = DoobieArticleRepository[F](xa)
      commentRepo = DoobieCommentRepository[F](xa)
      feedbackRepo = DoobieFeedbackRepository[F](xa)

      // Validators
      userValidation = UserValidation[F](userRepo)
      articleValidation = ArticleValidation[F](articleRepo)

      // Services
      userService = UserService[F](userRepo, userValidation)
      articleService = ArticleService[F](articleRepo, articleValidation)
      commentService = CommentService[F](commentRepo, articleValidation)

      // Auth
      authenticator = Auth.jwtAuthenticator[F, HMACSHA256](key, authRepo, userRepo)
      routeAuth = SecuredRequestHandler(authenticator)

      // Routes
      httpApp = Router(
        "/auth" -> UserEndpoints
          .endpoints[F, BCrypt, HMACSHA256](userService, BCrypt.syncPasswordHasher[F], routeAuth),
        "/article" -> ArticleEndpoints.endpoints(articleService, routeAuth),
        "/comment" -> CommentEndpoints.endpoints(commentService, routeAuth),
        "/feedback" -> FeedbackEndpoints.endpoints(feedbackRepo, routeAuth)
      ).orNotFound

      // Database + Migrate
      _ <- Resource.eval(DatabaseConfig.initializeDb(conf.db))

      // Server binding
      server <- BlazeServerBuilder[F](serverEc)
        .bindHttp(conf.server.port, conf.server.host)
        .withHttpApp(httpApp)
        .resource
    } yield server

  def run(args: List[String]): IO[ExitCode] = createServer.use(_ => IO.never).as(ExitCode.Success)
}