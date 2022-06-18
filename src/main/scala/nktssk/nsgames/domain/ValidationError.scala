package nktssk.nsgames.domain

sealed trait ValidationError extends Product with Serializable

case object PetNotFoundError extends ValidationError

case object UserNotFoundError extends ValidationError

case object OrderNotFoundError extends ValidationError

case class UserAlreadyExistsError(phoneNumber: String) extends ValidationError

case class UserAuthenticationFailedError(phoneNumber: String) extends ValidationError
