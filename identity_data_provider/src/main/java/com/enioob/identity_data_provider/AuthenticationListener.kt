package com.enioob.identity_data_provider

import com.enioob.identity_data_provider.model.IdpUser

interface AuthenticationListener {
  fun onLoadingStatusChanged(isLoading : Boolean)
  fun onLogIn()
  fun onRegister(user : IdpUser)
  fun onLogOut()
  fun onPasswordReset()
  fun onVerificationEmailResent()
  fun onEmailVerified(user : IdpUser)
  fun onForgotPasswordMailSent()
  fun onError(message : String)
  fun onTokensRefreshed()
  fun onForgottenPasswordReset()
}