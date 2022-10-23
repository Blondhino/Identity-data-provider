package com.enioob.identity_data_provider.repo

import androidx.activity.ComponentActivity
import com.enioob.identity_data_provider.ExchangeTokenRequest
import com.enioob.identity_data_provider.GoogleTokenResponse
import com.enioob.identity_data_provider.getStringResourceByName
import com.enioob.identity_data_provider.model.LoginResponse
import com.enioob.identity_data_provider.networking.NetworkingModule
import com.enioob.identity_data_provider.utils.*

internal class IdpRepositoryImpl(val backendUrl: String, val componentActivity: ComponentActivity) : IdpRepository {
  private val networking = NetworkingModule(backendUrl)
  val safeApiCall = SafeApiCall()
  
  override suspend fun exchangeTokens(sdkToken: String, provider: AuthProvider): Result<LoginResponse> {
    if (provider == AuthProvider.FACEBOOK) {
      return safeApiCall {
        networking.api.exchangeTokens(provider = provider.url, ExchangeTokenRequest(sdkToken))
      }
    } else if (provider == AuthProvider.GOOGLE) {
      safeApiCall {
        networking.googleApi.getAccessToken(
          grantType = GRANT_TYPE,
          clientId = getStringResourceByName(componentActivity, GOOGLE_CLIENT_ID, ""),
          clientSecret = getStringResourceByName(componentActivity, GOOGLE_CLIENT_SECRET, ""),
          redirectUrl = "",
          code = sdkToken
        )
      }
        .onSuccess { return exchangeGoogleSdkAccessToken(it) }
        .onFailure { return Result.failure(it) }
    }
    return Result.failure(IdpCredentialsException(UNKNOWN_ERROR))
  }
  
  private suspend fun exchangeGoogleSdkAccessToken(googleTokenResponse: GoogleTokenResponse): Result<LoginResponse> {
    return safeApiCall {
      networking.api.exchangeTokens(
        provider = AuthProvider.GOOGLE.url,
        ExchangeTokenRequest(googleTokenResponse.access_token ?: "")
      )
    }
  }
}