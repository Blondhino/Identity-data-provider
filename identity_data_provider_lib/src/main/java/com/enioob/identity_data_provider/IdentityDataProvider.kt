package com.enioob.identity_data_provider

import androidx.activity.ComponentActivity
import com.enioob.identity_data_provider.com.enioob.identity_data_provider.LoginMutation
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
  private var onLogInListener: OnLogInListener? = null
  private var onRegisterListener: OnRegisterListener? = null
  private var onLogoutListener: OnLogoutListener? = null
  private var onPasswordResetListener: OnPasswordResetListener? = null
  private var onVerificationEmailResentListener: OnVerificationEmailResentListener? = null
  private var onEmailVerifiedListener: OnEmailVerifiedListener? = null
  private var onForgotPasswordMailSentListener: OnForgotPasswordMailSentListener? = null
  private var onErrorListener: OnErrorListener? = null
  private var onUserUpdatedListener: OnUserUpdatedListener? = null
  private var onTokensRefreshedListener: OnTokensRefreshedListener? = null
  private var onForgottenPasswordResetListener: OnForgottenPasswordResetListener? = null
  private var onUserDeletedListener: OnUserDeletedListener? = null
  
  fun setOnLoginListener(onLogInListener: OnLogInListener) {
    this.onLogInListener = onLogInListener
  }
  
  fun setOnRegisterListener(onRegisterListener: OnRegisterListener) {
    this.onRegisterListener = onRegisterListener
  }
  
  fun setOnLogoutListener(onLogoutListener: OnLogoutListener) {
    this.onLogoutListener = onLogoutListener
  }
  
  fun setOnPasswordResetListener(onPasswordResetListener: OnPasswordResetListener) {
    this.onPasswordResetListener = onPasswordResetListener
  }
  
  fun setOnVerificationEmailResentListener(onVerificationEmailResentListener: OnVerificationEmailResentListener) {
    this.onVerificationEmailResentListener = onVerificationEmailResentListener
  }
  
  fun setOnEmailVerifiedListener(onEmailVerifiedListener: OnEmailVerifiedListener) {
    this.onEmailVerifiedListener = onEmailVerifiedListener
  }
  
  fun setOnForgotPasswordMailSentListener(onForgotPasswordMailSentListener: OnForgotPasswordMailSentListener) {
    this.onForgotPasswordMailSentListener = onForgotPasswordMailSentListener
  }
  
  fun setOnErrorListener(onErrorListener: OnErrorListener) {
    this.onErrorListener = onErrorListener
  }
  
  fun setOnUserUpdatedListener(onUserUpdatedListener: OnUserUpdatedListener) {
    this.onUserUpdatedListener = onUserUpdatedListener
  }
  
  fun setOnTokensRefreshedListener(onTokensRefreshedListener: OnTokensRefreshedListener) {
    this.onTokensRefreshedListener = onTokensRefreshedListener
  }
  
  fun setOnForgottenPasswordResetListener(onForgottenPasswordResetListener: OnForgottenPasswordResetListener) {
    this.onForgottenPasswordResetListener = onForgottenPasswordResetListener
  }
  
  fun setOnUserDeletedListener(onUserDeletedListener: OnUserDeletedListener) {
    this.onUserDeletedListener = onUserDeletedListener
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
    onLogoutListener?.onLogOut()
  }
  
  override fun isUserAuthenticated(): Boolean {
    return idpRepository.isUserAuthenticated()
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
  
  override fun registerByEmailAndPassword(email: String, password: String, confirmedPassword: String) {
    CoroutineScope(Dispatchers.Main).launch {
      idpRepository.registerByEmailAndPassword(email, password, confirmedPassword)
        .onSuccess { onRegisterListener?.onRegister(it) }
        .onFailure { onErrorListener?.onError(it.message.orEmpty()) }
    }
  }
  
  override suspend fun loginByEmailAndPassword(email: String, password: String): Result<LoginMutation.Data> {
    return idpRepository.loginByEmailAndPassword(email, password).onSuccess {
      idpRepository.saveTokens(LoginResponse(accessToken = it.login.accessToken, refreshToken = it.login.refreshToken))
    }
  }
  
  override fun resetLoggedUserPassword(password: String, confirmedPassword: String, oldPassword: String) {
    CoroutineScope(Dispatchers.Main).launch {
      idpRepository.resetLoggedUserPassword(password, confirmedPassword, oldPassword)
        .onSuccess { onPasswordResetListener?.onPasswordReset() }.onFailure { onErrorListener?.onError(it.message.orEmpty()) }
    }
  }
  
  override fun verifyEmail(token: String) {
    CoroutineScope(Dispatchers.Main).launch {
      idpRepository.verifyEmail(token).onSuccess { onEmailVerifiedListener?.onEmailVerified(it) }
        .onFailure { onErrorListener?.onError(it.message.orEmpty()) }
    }
  }
  
  override fun forgotPassword(email: String) {
    CoroutineScope(Dispatchers.Main).launch {
      idpRepository.forgotPassword(email).onSuccess { onForgotPasswordMailSentListener?.onForgotPasswordMailSent() }
        .onFailure { onErrorListener?.onError(it.message.orEmpty()) }
    }
  }
  
  override fun resetForgottenPassword(token: String, password: String, confirmedPassword: String) {
    CoroutineScope(Dispatchers.Main).launch {
      idpRepository.resetForgottenPassword(token, password, confirmedPassword)
        .onSuccess { onForgottenPasswordResetListener?.onForgottenPasswordReset() }.onFailure {
          onErrorListener?.onError(
            it.message.orEmpty
              ()
          )
        }
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
        onTokensRefreshedListener?.onTokensRefreshed()
      }.onFailure { onErrorListener?.onError(it.message.orEmpty()) }
    }
  }
  
  override fun deleteUser(userId: String) {
    CoroutineScope(Dispatchers.Main).launch {
      idpRepository.deleteUser(userId)
        .onSuccess { onUserDeletedListener?.onUserDeleted() }
        .onFailure { onErrorListener?.onError(it.message.orEmpty()) }
    }
  }
  
  override fun updateUser(
    id: String,
    email: String?,
    phone: String?,
    name: String?,
    nickName: String?,
    avatarUrl: String?,
    claims: String?,
    status: String?
  ) {
    CoroutineScope(Dispatchers.Main).launch {
      idpRepository.updateUser(id, email, phone, name, nickName, avatarUrl, claims, status)
        .onSuccess { onUserUpdatedListener?.onUserUpdated(it) }
        .onFailure { onErrorListener?.onError(it.message.orEmpty()) }
    }
  }
  
  override fun resendVerificationEmail(email: String) {
    CoroutineScope(Dispatchers.Main).launch {
      idpRepository.resendVerificationEmail(email).onSuccess { onForgottenPasswordResetListener?.onForgottenPasswordReset() }
        .onFailure { onErrorListener?.onError(it.message.toString()) }
    }
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