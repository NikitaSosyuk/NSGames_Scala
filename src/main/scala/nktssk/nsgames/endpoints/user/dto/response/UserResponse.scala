package nktssk.nsgames.endpoints.user.dto.response

import nktssk.nsgames.domain.users.models.{User, UserState}

case class UserResponse(
                         firstname: String,
                         lastname: String,
                         phoneNumber: String,
                         state: UserState
                       )
object UserResponse {
  def from(user: User): UserResponse = {
    UserResponse(user.firstName, user.lastName, user.phoneNumber, user.state)
  }
}
