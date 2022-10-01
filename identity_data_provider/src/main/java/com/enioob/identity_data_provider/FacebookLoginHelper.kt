package com.enioob.identity_data_provider

interface FacebookLoginHelper {
  fun loginByFacebook(
    onProcessStart: () -> Unit = {},
    onProcessEnd: () -> Unit = {},
    onSuccess: (sdkToken: String) -> Unit = {},
    onError : (error : String) -> Unit = {}
  )
  
  fun isUserAuthenticatedWithFacebook() : Boolean
  fun facebookLogout()
}