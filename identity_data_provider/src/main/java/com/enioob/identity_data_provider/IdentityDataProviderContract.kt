package com.enioob.identity_data_provider

import com.facebook.login.widget.LoginButton

internal interface IdentityDataProviderContract {
  fun loginWithFacebook(facebookLoginButton: LoginButton)
  fun loginWithGoogle()
  fun logout()
  fun isUserAuthenticated():Boolean
}