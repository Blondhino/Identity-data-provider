package com.enioob.identity_data_provider

import okhttp3.ResponseBody
import retrofit2.http.*

internal interface GoogleApi {
  
  @FormUrlEncoded
  @POST("oauth2/v4/token")
  suspend fun getAccessToken(
    @Field("grant_type") grantType: String,
    @Field("client_id") clientId: String,
    @Field("client_secret") clientSecret : String,
    @Field("redirect_uri") redirectUrl : String,
    @Field("code") code : String
  ): GoogleTokenResponse
}

internal data class GoogleTokenResponse(
  val access_token : String?,
  val expires_in : Int?,
  val refreshToken : String?,
  val scope : String?,
  val token_type : String?,
  val id_token : String?
)


