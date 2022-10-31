package com.enioob.identity_data_provider.repo

import com.enioob.identity_data_provider.com.enioob.identity_data_provider.LoginMutation
import com.enioob.identity_data_provider.com.enioob.identity_data_provider.RegisterMutation
import com.enioob.identity_data_provider.model.LoginResponse
import com.enioob.identity_data_provider.utils.AuthProvider

internal interface IdpRepository {
  suspend fun exchangeTokens(sdkToken: String, provider: AuthProvider): Result<LoginResponse>
  fun saveTokens(loginResponse: LoginResponse)
  fun isUserAuthenticated(): Boolean
  fun logout()
  fun getAccessToken(): String
  fun getRefreshToken(): String
  suspend fun registerByEmailAndPassword(
    email: String,
    password: String,
    confirmedPassword: String
  ): Result<RegisterMutation.Data>
  suspend fun loginByEmailAndPassword(email: String, password: String) : Result<LoginMutation.Data>
}