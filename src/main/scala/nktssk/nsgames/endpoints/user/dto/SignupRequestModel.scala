package nktssk.nsgames.endpoints.user.dto

final case class SignupRequestModel(
                                     firstname: String,
                                     lastname: String,
                                     phoneNumber: String,
                                     email: String,
                                     password: String
                                   )