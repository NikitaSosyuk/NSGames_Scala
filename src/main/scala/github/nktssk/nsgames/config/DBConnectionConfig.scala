package github.nktssk.nsgames.config

import cats.effect.{Async, Blocker, ContextShift, Resource}
import doobie.hikari.HikariTransactor

import scala.concurrent.ExecutionContext
import scala.language.higherKinds

final case class DBConnectionConfig(poolSize: Int)
final case class DBConfig(user: String, password: String, url: String, driver: String, connections: DBConnectionConfig)

object DatabaseConfig {
  def dbTransactor[F[_] : Async : ContextShift](dbc: DBConfig,
                                                connEc: ExecutionContext,
                                                blocker: Blocker,
                                               ): Resource[F, HikariTransactor[F]] =
    HikariTransactor
      .newHikariTransactor[F](dbc.driver, dbc.url, dbc.user, dbc.password, connEc, blocker)
}