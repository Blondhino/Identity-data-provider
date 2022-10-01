package com.enioob.identity_data_provider

import android.util.Log
import androidx.activity.ComponentActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


internal class IdpRepository(private val backendBaseUrl: String) : IdentityDataProviderContract {
  
  var onAuthChangedListener: ((isAuthenticated: Boolean) -> Unit) = {}
  var onAuthProcessActivityChanged: ((isAuthProcessActive: Boolean) -> Unit) = {}
  private lateinit var activity: ComponentActivity
  private val networking = NetworkingModule(backendBaseUrl)
  private lateinit var facebookLoginHelper: FacebookLoginHelper
  private lateinit var googleLoginHelper: GoogleLoginHelper
  
  
  override fun registerHostingActivity(componentActivity: ComponentActivity) {
    this.activity = componentActivity
    facebookLoginHelper = FacebookLoginHelperImpl(this.activity)
    googleLoginHelper = GoogleLoginHelperImpl(this.activity)
    
    
  }
  
  
  override fun loginWithFacebook() {
    facebookLoginHelper.loginByFacebook(
      onProcessStart = { onAuthProcessActivityChanged(true) },
      onProcessEnd = { onAuthProcessActivityChanged(false) },
      onSuccess = { sdkToken ->
        onAuthChangedListener(true)
        exchangeToken(sdkToken, AuthProvider.FACEBOOK.url)
      }
    )
  }
  
  override fun loginWithGoogle() {
    googleLoginHelper.registerListener(object : GoogleLoginListener {
      override fun onSuccess(sdkToken: String) {
        onAuthChangedListener(true)
        exchangeToken(sdkToken, AuthProvider.GOOGLE.url)
      }
      
      override fun onError(error: String) {}
      
      override fun onLoginProcessStart() {
        onAuthProcessActivityChanged(true)
      }
      
      override fun onLoginProcessEnd() {
        onAuthProcessActivityChanged(false)
      }
      
    })
    googleLoginHelper.loginByGoogle()
  }
  
  override fun logout() {
    onAuthProcessActivityChanged(true)
    googleLoginHelper.googleLogout()
    facebookLoginHelper.facebookLogout()
    onAuthChangedListener.invoke(false)
    onAuthProcessActivityChanged(false)
  }
  
  override fun isUserAuthenticated(): Boolean {
    
    if (facebookLoginHelper.isUserAuthenticatedWithFacebook() || googleLoginHelper.isUserAuthenticatedWithGoogle()) {
      return true
    }
    return false
  }
  
  
  private fun exchangeToken(token: String, provider: String) = CoroutineScope(Dispatchers.IO).launch {
    try {
      val resp = networking.api.exchangeTokens(provider, ExchangeTokenRequest(token))
      onAuthProcessActivityChanged.invoke(false)
    } catch (e: Exception) {
      Log.d("error:::", e.message.toString())
    }
  }
  
  
}
