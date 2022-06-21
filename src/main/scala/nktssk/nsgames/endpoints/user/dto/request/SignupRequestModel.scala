package nktssk.nsgames.endpoints.user.dto.request

final case class SignupRequestModel(
                                     firstname: String,
                                     lastname: String,
                                     phoneNumber: String,
                                     email: String,
                                     password: String
                                   )
