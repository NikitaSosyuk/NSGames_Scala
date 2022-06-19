package nktssk.nsgames.domain

trait ValidationError extends Product with Serializable

// User
case object UserNotFoundError extends ValidationError
case class UserAlreadyExistsError(phoneNumber: String) extends ValidationError
case class UserAuthenticationFailedError(phoneNumber: String) extends ValidationError

// Article
case object ArticleHiddenError extends ValidationError
case object ArticleNotFoundError extends ValidationError