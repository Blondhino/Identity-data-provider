package com.enioob.identity_data_provider

import androidx.activity.ComponentActivity
import com.enioob.identity_data_provider.com.enioob.identity_data_provider.LoginMutation


internal interface IdentityDataProviderContract {
  fun loginWithFacebook()
  fun loginWithGoogle()
  fun logout()
  fun isUserAuthenticated(): Boolean
  fun registerHostingActivity(componentActivity: ComponentActivity)
  fun registerByEmailAndPassword(email: String, password: String, confirmedPassword: String)
  suspend fun loginByEmailAndPassword(email: String, password: String) : Result<LoginMutation.Data>
  fun resetLoggedUserPassword(password: String, confirmedPassword: String, oldPassword: String)
  fun resendVerificationEmail(email: String)
  fun verifyEmail(token: String)
  fun forgotPassword(email: String)
  fun resetForgottenPassword(token: String, password: String, confirmedPassword: String)
  fun refreshTokens()
  fun deleteUser(userId: String)
  fun updateUser(
    id: String,
    email: String? = null,
    phone: String? = null,
    name: String? = null,
    nickName: String? = null,
    avatarUrl: String? = null,
    claims: String? = null,
    status: String? = null,
  )
}