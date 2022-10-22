package com.enioob.identity_data_provider.model

import androidx.annotation.Keep
@Keep
data class LoginResponse(
  val token: String = "",
  val refreshToken: String = "",
)