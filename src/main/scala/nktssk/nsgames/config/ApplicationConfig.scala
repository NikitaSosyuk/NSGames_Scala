package nktssk.nsgames.config

final case class ServerConfig(host: String, port: Int)
final case class ApplicationConfig(db:DBConfig, server: ServerConfig)