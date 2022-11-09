package com.enioob.identity_data_provider.model

import androidx.annotation.Keep
@Keep
internal data class LoginResponse(
  val accessToken: String = "",
  val refreshToken: String = "",
)