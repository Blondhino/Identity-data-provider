package com.enioob.identity_data_provider

import androidx.activity.ComponentActivity

internal interface IdentityDataProviderContract {
  fun loginWithFacebook()
  fun loginWithGoogle()
  fun logout()
  fun isUserAuthenticated():Boolean
  fun registerHostingActivity(componentActivity: ComponentActivity)
  fun registerAuthenticationListener(authenticationListener: AuthenticationListener)
  fun registerByEmailAndPassword(email : String, password : String, confirmedPassword : String)
  fun loginByEmailAndPassword(email : String, password: String)
}