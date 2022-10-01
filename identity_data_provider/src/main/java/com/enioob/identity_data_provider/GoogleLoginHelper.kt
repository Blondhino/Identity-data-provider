package com.enioob.identity_data_provider

interface GoogleLoginHelper {
  fun loginByGoogle()
  fun registerListener(listener : GoogleLoginListener)
  fun isUserAuthenticatedWithGoogle() : Boolean
  fun googleLogout()
}

interface GoogleLoginListener {
  fun onSuccess(sdkToken : String)
  fun onError(error : String)
  fun onLoginProcessStart()
  fun onLoginProcessEnd()
}