package nktssk.nsgames.domain.users.models

import enumeratum._

import scala.collection.immutable

sealed trait UserState extends EnumEntry

case object UserState extends Enum[UserState] with CirceEnum[UserState] {
  case object Active extends UserState
  case object Blocked extends UserState
  case object Invalid extends UserState

  val values: immutable.IndexedSeq[UserState] = findValues
}