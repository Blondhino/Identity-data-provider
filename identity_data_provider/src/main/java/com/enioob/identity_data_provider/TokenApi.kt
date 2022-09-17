package com.enioob.identity_data_provider

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

internal interface TokenApi {

  @POST("social/{provider}/exchange-token")
  suspend fun exchangeTokens(@Path("provider") provider : String, @Body exchangeTokenRequest: ExchangeTokenRequest) : Any
}

internal data class ExchangeTokenRequest(
  val token : String
)

data class Response(
  val token: String
)

