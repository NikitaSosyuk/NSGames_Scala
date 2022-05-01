package github.nktssk.nsgames.config

final case class ServerConfig(host: String, post: Int)
final case class ApplicationConfig(database:DBConfig, server: ServerConfig)