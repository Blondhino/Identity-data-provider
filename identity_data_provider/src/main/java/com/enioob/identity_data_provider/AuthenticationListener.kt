package com.enioob.identity_data_provider

interface AuthenticationListener {
  fun onLoadingStatusChanged(isLoading : Boolean)
  fun onLogIn()
  fun onRegister()
  fun onLogOut()
  fun onError(message : String)
}