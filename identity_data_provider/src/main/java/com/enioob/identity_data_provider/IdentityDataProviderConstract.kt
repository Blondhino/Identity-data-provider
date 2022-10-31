package com.enioob.identity_data_provider

import androidx.activity.ComponentActivity

internal interface IdentityDataProviderConstract {
  fun loginWithFacebook()
  fun loginWithGoogle()
  fun logout()
  fun isUserAuthenticated():Boolean
  fun registerHostingActivity(componentActivity: ComponentActivity)
  fun registerAuthenticationListener(authenticationListener: AuthenticationListener)
}