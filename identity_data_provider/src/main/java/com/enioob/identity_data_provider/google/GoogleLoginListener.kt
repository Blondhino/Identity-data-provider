package com.enioob.identity_data_provider.google

internal interface GoogleLoginListener {
  fun onSuccess(sdkToken : String)
  fun onError(error : String)
  fun onLoginProcessStart()
  fun onLoginProcessEnd()
}