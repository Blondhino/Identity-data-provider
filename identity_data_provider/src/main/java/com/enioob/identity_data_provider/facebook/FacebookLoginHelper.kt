package com.enioob.identity_data_provider.facebook

interface FacebookLoginHelper {
  fun loginByFacebook()
  fun isUserAuthenticatedWithFacebook() : Boolean
  fun facebookLogout()
  fun registerFacebookLoginListener(facebookLoginListener: FacebookLoginListener)
}