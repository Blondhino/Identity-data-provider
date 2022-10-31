package com.enioob.identity_data_provider.repo

import com.enioob.identity_data_provider.com.enioob.identity_data_provider.*
import com.enioob.identity_data_provider.model.IdpUser
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
    email: String, password: String, confirmedPassword: String
  ): Result<IdpUser>
  
  suspend fun loginByEmailAndPassword(email: String, password: String): Result<LoginMutation.Data>
  suspend fun resetLoggedUserPassword(
    password: String,
    confirmedPassword: String,
    oldPassword: String
  ): Result<ResetLoggedUserPasswordMutation.Data>
  
  suspend fun resendVerificationEmail(email: String): Result<ResendVerificationEmailMutation.Data>
  suspend fun verifyEmail(token: String): Result<IdpUser>
  suspend fun forgotPassword(email: String): Result<ForgotPasswordMutation.Data>
  suspend fun resetForgottenPassword(
    token: String,
    password: String,
    confirmedPassword: String
  ): Result<ResetForgottenPasswordMutation.Data>
  
  suspend fun refreshTokens() : Result<RefreshTokenMutation.Data>
  suspend fun deleteUser(userId : String) : Result<UserDeleteMutation.Data>
}