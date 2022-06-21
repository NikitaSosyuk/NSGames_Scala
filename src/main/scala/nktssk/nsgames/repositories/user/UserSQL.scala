package nktssk.nsgames.repositories.user

import doobie.{Meta, Query0, Update0}
import nktssk.nsgames.domain.users.models.{Role, User, UserState}
import cats.syntax.all._
import doobie.implicits._
import io.circe.parser.decode
import io.circe.syntax._


object UserSQL {
  implicit val StatusMeta: Meta[UserState] =
    Meta[String].imap(UserState.withName)(_.entryName)

  implicit val roleMeta: Meta[Role] =
    Meta[String].imap(decode[Role](_).leftMap(throw _).merge)(_.asJson.toString)

  def insert(user: User): Update0 = sql"""
    INSERT INTO USERS (FIRST_NAME, LAST_NAME, PHONE_NUMBER, EMAIL, PASSWORD, STATE, ROLE)
    VALUES (${user.firstName}, ${user.lastName}, ${user.phoneNumber}, ${user.email}, ${user.password}, ${user.state}, ${user.role})
  """.update

  def select(userId: Long): Query0[User] = sql"""
    SELECT ID, FIRST_NAME, LAST_NAME, PHONE_NUMBER, CONFIRM_CODE, PASSWORD, EMAIL, ROLE, STATE
    FROM USERS
    WHERE ID = $userId
  """.query

  def byEmail(email: String): Query0[User] = sql"""
    SELECT ID,FIRST_NAME, LAST_NAME, PHONE_NUMBER, EMAIL, PASSWORD, ROLE, STATE
    FROM USERS
    WHERE EMAIL = $email
  """.query[User]

  def byPhoneNumber(phoneNumber: String): Query0[User] = sql"""
    SELECT ID, FIRST_NAME, LAST_NAME, PHONE_NUMBER, CONFIRM_CODE, PASSWORD, EMAIL, ROLE, STATE
    FROM USERS
    WHERE PHONE_NUMBER = $phoneNumber
  """.query[User]

  def update(phoneNumber: String, code: String): Update0 = sql"""
    UPDATE USERS
    SET CONFIRM_CODE = ${code},
    WHERE PHONE_NUMBER = ${phoneNumber}
  """.update
}
