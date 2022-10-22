package com.enioob.identity_data_provider.facebook

interface FacebookLoginListener {
  fun onSuccess(sdkToken : String)
  fun onError(error : String)
  fun onLoginProcessStart()
  fun onLoginProcessEnd()
}