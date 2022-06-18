package nktssk.nsgames.config
import cats.syntax.functor._
import cats.effect.{Async, Blocker, ContextShift, Resource, Sync}
import doobie.hikari.HikariTransactor
import org.flywaydb.core.Flyway

import scala.concurrent.ExecutionContext

final case class DBConnectionConfig(poolSize: Int)
final case class DBConfig(
                           user: String,
                           password: String,
                           url: String,
                           driver: String,
                           connections: DBConnectionConfig
                         )

object DatabaseConfig {
  def dbTransactor[F[_] : Async : ContextShift](
    dbc: DBConfig,
    connEc: ExecutionContext,
    blocker: Blocker
  ): Resource[F, HikariTransactor[F]] =
    HikariTransactor.newHikariTransactor[F](dbc.driver, dbc.url, dbc.user, dbc.password, connEc, blocker)

  def initializeDb[F[_]](
    cfg: DBConfig
  )(implicit S: Sync[F]): F[Unit] =
    S.delay {
      val fw: Flyway =
        Flyway
          .configure()
          .dataSource(cfg.url, cfg.user, cfg.password)
          .load()
      fw.migrate()
    }.as(())
}