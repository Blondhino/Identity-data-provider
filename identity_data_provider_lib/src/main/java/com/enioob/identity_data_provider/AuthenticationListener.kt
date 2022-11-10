package com.enioob.identity_data_provider

import com.enioob.identity_data_provider.model.IdpUser

interface OnLoadingStatusChangedListener {
  fun onLoadingStatusChanged(isLoading: Boolean)
}

interface OnLogInListener {
  fun onLogIn()
}

interface OnRegisterListener {
  fun onRegister(user: IdpUser)
}

interface OnLogoutListener {
  fun onLogOut()
}

interface OnPasswordResetListener {
  fun onPasswordReset()
}

interface OnVerificationEmailResentListener {
  fun onVerificationEmailResent()
}

interface OnEmailVerifiedListener {
  fun onEmailVerified(idpUser: IdpUser)
}

interface OnForgotPasswordMailSentListener {
  fun onForgotPasswordMailSent()
}

interface OnErrorListener {
  fun onError(error : String)
}

interface OnUserUpdatedListener {
  fun onUserUpdated(user: IdpUser)
}

interface OnTokensRefreshedListener {
  fun onTokensRefreshed()
}

interface OnForgottenPasswordResetListener {
  fun onForgottenPasswordReset()
}

interface OnUserDeletedListener {
  fun onUserDeleted()
}