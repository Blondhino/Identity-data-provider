package com.enioob.identity_data_provider

interface AuthenticationListener {
  fun onLoadingStatusChanged(isLoading : Boolean)
  fun onLogIn()
  fun onRegister()
  fun onLogOut()
  fun onPasswordReset()
  fun onError(message : String)
}