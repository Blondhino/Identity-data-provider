package com.enioob.identity_data_provider

import androidx.activity.ComponentActivity
import com.enioob.identity_data_provider.facebook.FacebookLoginHelper
import com.enioob.identity_data_provider.facebook.FacebookLoginHelperImpl
import com.enioob.identity_data_provider.facebook.FacebookLoginListener
import com.enioob.identity_data_provider.google.GoogleLoginHelperImpl
import com.enioob.identity_data_provider.google.GoogleLoginListener
import com.enioob.identity_data_provider.repo.IdpRepository
import com.enioob.identity_data_provider.repo.IdpRepositoryImpl
import com.enioob.identity_data_provider.utils.AuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class IdentityDataProvider(val backendUrl: String) : IdentityDataProviderConstract {
  private lateinit var googleLoginHelper: GoogleLoginHelper
  private lateinit var facebookLoginHelper: FacebookLoginHelper
  private lateinit var componentActivity: ComponentActivity
  private val idpRepository: IdpRepository by lazy { IdpRepositoryImpl(backendUrl, componentActivity) }
  private var authListener: AuthenticationListener? = null
  
  override fun loginWithFacebook() {
    facebookLoginHelper.loginByFacebook()
  }
  
  override fun loginWithGoogle() {
    googleLoginHelper.loginByGoogle()
  }
  
  override fun logout() {
    googleLoginHelper.googleLogout()
    facebookLoginHelper.facebookLogout()
    authListener?.onLogOut()
  }
  
  override fun isUserAuthenticated(): Boolean {
    return (googleLoginHelper.isUserAuthenticatedWithGoogle() || facebookLoginHelper.isUserAuthenticatedWithFacebook())
  }
  
  override fun registerHostingActivity(componentActivity: ComponentActivity) {
    this.componentActivity = componentActivity
    googleLoginHelper = GoogleLoginHelperImpl(componentActivity)
    facebookLoginHelper = FacebookLoginHelperImpl(componentActivity)
    registerGoogleAuthListeners()
    registerFacebookAuthListeners()
  }
  
  private fun registerFacebookAuthListeners(){
    facebookLoginHelper.registerFacebookLoginListener(object : FacebookLoginListener {
      override fun onSuccess(sdkToken: String) {
        exchangeTokens(sdkToken, AuthProvider.FACEBOOK)
      }
      
      override fun onError(error: String) {
          authListener?.onError(error)
      }
      
      override fun onLoginProcessStart() {
        authListener?.onLoadingStatusChanged(true)
      }
      
      override fun onLoginProcessEnd() {
        authListener?.onLoadingStatusChanged(false)
      }
      
    })
  }
  
  private fun exchangeTokens(sdkToken: String, authProvider: AuthProvider) = CoroutineScope(Dispatchers.Main).launch{
    idpRepository.exchangeTokens(sdkToken, authProvider)
      .onSuccess { authListener?.onLogIn() }
      .onFailure {
        authListener?.onError(it.message.toString())
        
      }
  }
  
  override fun registerAuthenticationListener(authenticationListener: AuthenticationListener) {
    authListener = authenticationListener
  }
  
  private fun registerGoogleAuthListeners() {
    
    googleLoginHelper.registerListener(object : GoogleLoginListener {
      override fun onSuccess(sdkToken: String) {
        exchangeTokens(sdkToken, AuthProvider.GOOGLE)
      }
      
      override fun onError(error: String) {
        authListener?.onError(error)
      }
      
      override fun onLoginProcessStart() {
        authListener?.onLoadingStatusChanged(true)
      }
      
      override fun onLoginProcessEnd() {
        authListener?.onLoadingStatusChanged(false)
      }
      
    })
  }
}