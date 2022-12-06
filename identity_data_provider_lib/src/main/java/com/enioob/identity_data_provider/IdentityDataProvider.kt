package com.enioob.identity_data_provider

import androidx.activity.ComponentActivity
import com.enioob.identity_data_provider.com.enioob.identity_data_provider.*
import com.enioob.identity_data_provider.facebook.FacebookLoginHelper
import com.enioob.identity_data_provider.facebook.FacebookLoginHelperImpl
import com.enioob.identity_data_provider.facebook.FacebookLoginListener
import com.enioob.identity_data_provider.google.GoogleLoginHelperImpl
import com.enioob.identity_data_provider.google.GoogleLoginListener
import com.enioob.identity_data_provider.model.IdpUser
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
  private var onLogInListener: OnLogInListener? = null
  private var onErrorListener: OnErrorListener? = null
  
  
  fun setOnLoginListener(onLogInListener: OnLogInListener) {
    this.onLogInListener = onLogInListener
  }
  
  fun setOnErrorListener(onErrorListener: OnErrorListener) {
    this.onErrorListener = onErrorListener
  }
  
  
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
  }
  
  override fun isUserAuthenticated(): Boolean {
    return idpRepository.isUserAuthenticated()
  }
  
  override fun getAccessToken(): String {
    return idpRepository.getAccessToken()
  }
  
  override fun getRefreshToken(): String {
    return idpRepository.getRefreshToken()
  }
  
  override fun registerHostingActivity(componentActivity: ComponentActivity) {
    this.componentActivity = componentActivity
    googleLoginHelper = GoogleLoginHelperImpl(componentActivity)
    facebookLoginHelper = FacebookLoginHelperImpl(componentActivity)
    registerGoogleonErrorListeners()
    registerFacebookonErrorListeners()
  }
  
  private fun registerFacebookonErrorListeners() {
    facebookLoginHelper.registerFacebookLoginListener(object : FacebookLoginListener {
      override fun onSuccess(sdkToken: String) {
        exchangeTokens(sdkToken, AuthProvider.FACEBOOK)
      }
      
      override fun onError(error: String) {
        onErrorListener?.onError(error)
      }
      
      override fun onLoginProcessStart() {}
      
      override fun onLoginProcessEnd() {}
      
    })
  }
  
  private fun exchangeTokens(sdkToken: String, authProvider: AuthProvider) = CoroutineScope(Dispatchers.Main).launch {
    idpRepository.exchangeTokens(sdkToken, authProvider).onSuccess {
      onLogInListener?.onLogIn()
      idpRepository.saveTokens(it)
    }.onFailure {
      onErrorListener?.onError(it.message.toString())
      
    }
  }
  
  override suspend fun registerByEmailAndPassword(email: String, password: String, confirmedPassword: String): Result<IdpUser> {
    return idpRepository.registerByEmailAndPassword(email, password, confirmedPassword)
  }
  
  override suspend fun loginByEmailAndPassword(email: String, password: String): Result<LoginMutation.Data> {
    return idpRepository.loginByEmailAndPassword(email, password).onSuccess {
      idpRepository.saveTokens(LoginResponse(accessToken = it.login.accessToken, refreshToken = it.login.refreshToken))
    }
  }
  
  override suspend fun resetLoggedUserPassword(
    password: String,
    confirmedPassword: String,
    oldPassword: String
  ): Result<ResetLoggedUserPasswordMutation.Data> {
    return idpRepository.resetLoggedUserPassword(password, confirmedPassword, oldPassword)
  }
  
  override suspend fun verifyEmail(token: String): Result<IdpUser> {
    return idpRepository.verifyEmail(token)
  }
  
  override suspend fun forgotPassword(email: String): Result<ForgotPasswordMutation.Data> {
    return idpRepository.forgotPassword(email)
  }
  
  override suspend fun resetForgottenPassword(token: String, password: String, confirmedPassword: String): Result<ResetForgottenPasswordMutation.Data> {
    return idpRepository.resetForgottenPassword(token, password, confirmedPassword)
  }
  
  override suspend fun refreshTokens(): Result<RefreshTokenMutation.Data> {
    return idpRepository.refreshTokens().onSuccess {
      idpRepository.saveTokens(
        LoginResponse(
          accessToken = it.refresh_token.accessToken, refreshToken = it.refresh_token.refreshToken
        )
      )
    }
  }
  
  override suspend fun deleteUser(userId: String): Result<UserDeleteMutation.Data> {
    return idpRepository.deleteUser(userId)
  }
  
  override suspend fun updateUser(
    id: String,
    email: String?,
    phone: String?,
    name: String?,
    nickName: String?,
    avatarUrl: String?,
    claims: String?,
    status: String?
  ): Result<IdpUser> {
    return idpRepository.updateUser(id, email, phone, name, nickName, avatarUrl, claims, status)
  }
  
  override suspend fun resendVerificationEmail(email: String): Result<ResendVerificationEmailMutation.Data> {
    return idpRepository.resendVerificationEmail(email)
  }
  
  private fun registerGoogleonErrorListeners() {
    
    googleLoginHelper.registerListener(object : GoogleLoginListener {
      override fun onSuccess(sdkToken: String) {
        exchangeTokens(sdkToken, AuthProvider.GOOGLE)
      }
      
      override fun onError(error: String) {
        onErrorListener?.onError(error)
        onLoginProcessEnd()
      }
      
      override fun onLoginProcessStart() {}
      
      override fun onLoginProcessEnd() {}
      
    })
  }
}