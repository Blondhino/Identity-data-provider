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

class IdentityDataProvider(val backendUrl: String) : IdentityDataProviderContract {
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
    idpRepository.logout()
    authListener?.onLogOut()
  }
  
  override fun isUserAuthenticated(): Boolean {
    return idpRepository.isUserAuthenticated()
  }
  
  override fun registerHostingActivity(componentActivity: ComponentActivity) {
    this.componentActivity = componentActivity
    googleLoginHelper = GoogleLoginHelperImpl(componentActivity)
    facebookLoginHelper = FacebookLoginHelperImpl(componentActivity)
    registerGoogleAuthListeners()
    registerFacebookAuthListeners()
  }
  
  private fun registerFacebookAuthListeners() {
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
  
  private fun exchangeTokens(sdkToken: String, authProvider: AuthProvider) = CoroutineScope(Dispatchers.Main).launch {
    idpRepository.exchangeTokens(sdkToken, authProvider)
      .onSuccess {
        authListener?.onLoadingStatusChanged(false)
        authListener?.onLogIn()
        idpRepository.saveTokens(it)
      }
      .onFailure {
        authListener?.onLoadingStatusChanged(false)
        authListener?.onError(it.message.toString())
        
      }
  }
  
  override fun registerAuthenticationListener(authenticationListener: AuthenticationListener) {
    authListener = authenticationListener
  }
  
  override fun registerByEmailAndPassword(email: String, password: String, confirmedPassword: String) {
    CoroutineScope(Dispatchers.Main).launch {
      authListener?.onLoadingStatusChanged(true)
      idpRepository.registerByEmailAndPassword(email, password, confirmedPassword)
        .onSuccess { authListener?.onRegister() }
        .onFailure { authListener?.onError(it.message.orEmpty()) }
      authListener?.onLoadingStatusChanged(false)
    }
  }
  
  override fun loginByEmailAndPassword(email: String, password: String) {
    CoroutineScope(Dispatchers.Main).launch {
      authListener?.onLoadingStatusChanged(true)
      idpRepository.loginByEmailAndPassword(email, password)
        .onSuccess { authListener?.onLogIn() }
        .onFailure { authListener?.onError(it.message.orEmpty()) }
      authListener?.onLoadingStatusChanged(false)
    }
  }
  
  private fun registerGoogleAuthListeners() {
    
    googleLoginHelper.registerListener(object : GoogleLoginListener {
      override fun onSuccess(sdkToken: String) {
        exchangeTokens(sdkToken, AuthProvider.GOOGLE)
      }
      
      override fun onError(error: String) {
        authListener?.onError(error)
        onLoginProcessEnd()
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