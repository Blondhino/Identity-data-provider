package com.enioob.identity_data_provider

interface OnLogInListener {
  fun onLogIn()
}
interface OnErrorListener {
  fun onError(error : String)
}