package com.enioob.identity_data_provider.facebook

internal interface FacebookLoginHelper {
  fun loginByFacebook()
  fun isUserAuthenticatedWithFacebook() : Boolean
  fun facebookLogout()
  fun registerFacebookLoginListener(facebookLoginListener: FacebookLoginListener)
}