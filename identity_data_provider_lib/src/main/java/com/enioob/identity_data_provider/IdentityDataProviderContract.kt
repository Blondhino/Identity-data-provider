package com.enioob.identity_data_provider

import androidx.activity.ComponentActivity
import com.enioob.identity_data_provider.com.enioob.identity_data_provider.*
import com.enioob.identity_data_provider.model.IdpUser


internal interface IdentityDataProviderContract {
  fun loginWithFacebook()
  fun loginWithGoogle()
  fun logout()
  fun isUserAuthenticated(): Boolean
  fun getAccessToken() : String
  fun getRefreshToken() : String
  fun registerHostingActivity(componentActivity: ComponentActivity)
  suspend fun registerByEmailAndPassword(email: String, password: String, confirmedPassword: String): Result<IdpUser>
  suspend fun loginByEmailAndPassword(email: String, password: String): Result<LoginMutation.Data>
  suspend fun resetLoggedUserPassword(
    password: String,
    confirmedPassword: String,
    oldPassword: String
  ): Result<ResetLoggedUserPasswordMutation.Data>
  
  suspend fun resendVerificationEmail(email: String): Result<ResendVerificationEmailMutation.Data>
  suspend fun verifyEmail(token: String): Result<IdpUser>
  suspend fun forgotPassword(email: String): Result<ForgotPasswordMutation.Data>
  suspend fun resetForgottenPassword(token: String, password: String, confirmedPassword: String):
    Result<ResetForgottenPasswordMutation.Data>
  
  suspend fun refreshTokens(): Result<RefreshTokenMutation.Data>
  suspend fun deleteUser(userId: String): Result<UserDeleteMutation.Data>
  suspend fun updateUser(
    id: String,
    email: String? = null,
    phone: String? = null,
    name: String? = null,
    nickName: String? = null,
    avatarUrl: String? = null,
    claims: String? = null,
    status: String? = null,
  ): Result<IdpUser>
}