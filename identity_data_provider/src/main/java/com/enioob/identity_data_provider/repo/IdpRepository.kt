package com.enioob.identity_data_provider.repo

import com.enioob.identity_data_provider.model.LoginResponse
import com.enioob.identity_data_provider.utils.AuthProvider

internal interface IdpRepository {
  suspend fun exchangeTokens(sdkToken : String, provider : AuthProvider) : Result<LoginResponse>
  fun saveTokens(loginResponse: LoginResponse)
  fun isUserAuthenticated(): Boolean
  fun logout()
  fun getAccessToken():String
  fun getRefreshToken():String
}