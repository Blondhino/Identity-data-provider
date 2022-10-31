package com.enioob.identity_data_provider

import androidx.activity.ComponentActivity
import com.enioob.identity_data_provider.facebook.FacebookLoginHelper
import com.enioob.identity_data_provider.facebook.FacebookLoginHelperImpl
import com.enioob.identity_data_provider.facebook.FacebookLoginListener
import com.enioob.identity_data_provider.google.GoogleLoginHelperImpl
import com.enioob.identity_data_provider.google.GoogleLoginListener
import com.enioob.identity_data_provider.model.LoginResponse
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
    idpRepository.exchangeTokens(sdkToken, authProvider).onSuccess {
        authListener?.onLoadingStatusChanged(false)
        authListener?.onLogIn()
        idpRepository.saveTokens(it)
      }.onFailure {
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
      idpRepository.registerByEmailAndPassword(email, password, confirmedPassword).onSuccess { authListener?.onRegister(it) }
        .onFailure { authListener?.onError(it.message.orEmpty()) }
      authListener?.onLoadingStatusChanged(false)
    }
  }
  
  override fun loginByEmailAndPassword(email: String, password: String) {
    CoroutineScope(Dispatchers.Main).launch {
      authListener?.onLoadingStatusChanged(true)
      idpRepository.loginByEmailAndPassword(email, password).onSuccess {
          authListener?.onLogIn()
          idpRepository.saveTokens(LoginResponse(accessToken = it.login.accessToken, refreshToken = it.login.refreshToken))
        }.onFailure { authListener?.onError(it.message.orEmpty()) }
      authListener?.onLoadingStatusChanged(false)
    }
  }
  
  override fun resetLoggedUserPassword(password: String, confirmedPassword: String, oldPassword: String) {
    CoroutineScope(Dispatchers.Main).launch {
      idpRepository.resetLoggedUserPassword(password, confirmedPassword, oldPassword)
        .onSuccess { authListener?.onPasswordReset() }.onFailure { authListener?.onError(it.message.orEmpty()) }
    }
  }
  
  override fun verifyEmail(token: String) {
    CoroutineScope(Dispatchers.Main).launch {
      idpRepository.verifyEmail(token).onSuccess { authListener?.onEmailVerified(it) }
        .onFailure { authListener?.onError(it.message.orEmpty()) }
    }
  }
  
  override fun forgotPassword(email: String) {
    CoroutineScope(Dispatchers.Main).launch {
      idpRepository.forgotPassword(email).onSuccess { authListener?.onForgotPasswordMailSent() }
        .onFailure { authListener?.onError(it.message.orEmpty()) }
    }
  }
  
  override fun resetForgottenPassword(token: String, password: String, confirmedPassword: String) {
    CoroutineScope(Dispatchers.Main).launch {
      idpRepository.resetForgottenPassword(token, password, confirmedPassword)
        .onSuccess { authListener?.onForgottenPasswordReset() }.onFailure { authListener?.onError(it.message.orEmpty()) }
    }
  }
  
  override fun refreshTokens() {
    CoroutineScope(Dispatchers.Main).launch {
      idpRepository.refreshTokens().onSuccess {
          idpRepository.saveTokens(
            LoginResponse(
              accessToken = it.refresh_token.accessToken, refreshToken = it.refresh_token.refreshToken
            )
          )
          authListener?.onTokensRefreshed()
        }.onFailure { authListener?.onError(it.message.orEmpty()) }
    }
  }
  
  override fun deleteUser(userId: String) {
    CoroutineScope(Dispatchers.Main).launch {
      idpRepository.deleteUser(userId)
        .onSuccess { authListener?.onUserDeleted() }
        .onFailure { authListener?.onError(it.message.orEmpty()) }
    }
  }
  
  override fun resendVerificationEmail(email: String) {
    CoroutineScope(Dispatchers.Main).launch {
      idpRepository.resendVerificationEmail(email).onSuccess { authListener?.onForgottenPasswordReset() }
        .onFailure { authListener?.onError(it.message.toString()) }
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