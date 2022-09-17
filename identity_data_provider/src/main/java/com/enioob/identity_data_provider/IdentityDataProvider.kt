package com.enioob.identity_data_provider

import com.facebook.login.widget.LoginButton

class IdentityDataProvider(private val backendBaseUrl : String) : IdentityDataProviderContract {
  
  private val repo = IdpRepository(backendBaseUrl)
  var onAuthChangedListener: ((isAuthenticated : Boolean)->Unit) = {}
  var onAuthProcessActivityChanged: ((isAuthProcessActive : Boolean)->Unit) = {}
  
  init {
    repo.onAuthProcessActivityChanged = {onAuthProcessActivityChanged(it)}
    repo.onAuthChangedListener = {onAuthChangedListener(it)}
  }
  
  override fun loginWithFacebook(facebookLoginButton: LoginButton) = repo.loginWithFacebook(facebookLoginButton)
  
  override fun loginWithGoogle() = repo.loginWithGoogle()
  
  override fun logout() = repo.logout()
  
  override fun isUserAuthenticated(): Boolean = repo.isUserAuthenticated()
  
  
  
}