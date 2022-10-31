package com.enioob.identity_data_provider

import androidx.activity.ComponentActivity

internal interface IdentityDataProviderContract {
  fun loginWithFacebook()
  fun loginWithGoogle()
  fun logout()
  fun isUserAuthenticated(): Boolean
  fun registerHostingActivity(componentActivity: ComponentActivity)
  fun registerAuthenticationListener(authenticationListener: AuthenticationListener)
  fun registerByEmailAndPassword(email: String, password: String, confirmedPassword: String)
  fun loginByEmailAndPassword(email: String, password: String)
  fun resetLoggedUserPassword(password: String, confirmedPassword: String, oldPassword: String)
  fun resendVerificationEmail(email: String)
  fun verifyEmail(token : String)
}