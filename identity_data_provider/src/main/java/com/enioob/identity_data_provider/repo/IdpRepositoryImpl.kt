package com.enioob.identity_data_provider.repo

import androidx.activity.ComponentActivity
import com.enioob.identity_data_provider.ExchangeTokenRequest
import com.enioob.identity_data_provider.GoogleTokenResponse
import com.enioob.identity_data_provider.com.enioob.identity_data_provider.*
import com.enioob.identity_data_provider.com.enioob.identity_data_provider.type.LoginInputType
import com.enioob.identity_data_provider.com.enioob.identity_data_provider.type.RegistrationInputType
import com.enioob.identity_data_provider.com.enioob.identity_data_provider.type.ResetForgottenPasswordInputTypes
import com.enioob.identity_data_provider.com.enioob.identity_data_provider.type.ResetLoggedUserPasswordInputTypes
import com.enioob.identity_data_provider.getStringResourceByName
import com.enioob.identity_data_provider.model.IdpUser
import com.enioob.identity_data_provider.model.LoginResponse
import com.enioob.identity_data_provider.model.toIdpUser
import com.enioob.identity_data_provider.networking.NetworkingModule
import com.enioob.identity_data_provider.persistence.EncryptedPrefsModule
import com.enioob.identity_data_provider.utils.*

internal class IdpRepositoryImpl(private val backendUrl: String, private val componentActivity: ComponentActivity) :
  IdpRepository {
  private val networking = NetworkingModule(backendUrl, componentActivity.applicationContext)
  private val encryptedPrefs = EncryptedPrefsModule(componentActivity.applicationContext)
  val safeApiCall = SafeApiCall()
  val safeGraphCall = SafeGraphCall()
  
  override suspend fun exchangeTokens(sdkToken: String, provider: AuthProvider): Result<LoginResponse> {
    if (provider == AuthProvider.FACEBOOK) {
      return safeApiCall {
        networking.api.exchangeTokens(provider = provider.url, ExchangeTokenRequest(sdkToken))
      }
    } else if (provider == AuthProvider.GOOGLE) {
      safeApiCall {
        networking.googleApi.getAccessToken(
          grantType = GRANT_TYPE,
          clientId = getStringResourceByName(componentActivity, GOOGLE_CLIENT_ID, ""),
          clientSecret = getStringResourceByName(componentActivity, GOOGLE_CLIENT_SECRET, ""),
          redirectUrl = "",
          code = sdkToken
        )
      }.onSuccess { return exchangeGoogleSdkAccessToken(it) }.onFailure { return Result.failure(it) }
    }
    return Result.failure(IdpCredentialsException(UNKNOWN_ERROR))
  }
  
  override fun saveTokens(loginResponse: LoginResponse) {
    encryptedPrefs.saveToken(loginResponse.accessToken)
    encryptedPrefs.saveRefreshToken(loginResponse.refreshToken)
  }
  
  override fun isUserAuthenticated(): Boolean {
    return encryptedPrefs.getToken().isNotEmpty() && encryptedPrefs.getRefreshTokenToken().isNotEmpty()
  }
  
  override fun logout() {
    encryptedPrefs.saveToken(EMPTY)
    encryptedPrefs.saveRefreshToken(EMPTY)
  }
  
  override fun getAccessToken() = encryptedPrefs.getToken()
  
  override fun getRefreshToken() = encryptedPrefs.getRefreshTokenToken()
  
  override suspend fun registerByEmailAndPassword(
    email: String, password: String, confirmedPassword: String
  ): Result<IdpUser> {
    safeGraphCall {
      networking.apolloClient.mutation(RegisterMutation(RegistrationInputType(email, password, confirmedPassword)))
    }.onSuccess { return Result.success(it.register?.userFragment?.toIdpUser() ?: IdpUser()) }
      .onFailure { return Result.failure(it) }
    return Result.failure(Throwable(UNKNOWN_ERROR))
  }
  
  override suspend fun resetLoggedUserPassword(
    password: String, confirmedPassword: String, oldPassword: String
  ): Result<ResetLoggedUserPasswordMutation.Data> {
    return safeGraphCall {
      networking.apolloClient.mutation(
        ResetLoggedUserPasswordMutation(
          ResetLoggedUserPasswordInputTypes(
            password, confirmedPassword, oldPassword
          )
        )
      )
    }
  }
  
  override suspend fun resendVerificationEmail(email: String): Result<ResendVerificationEmailMutation.Data> {
    return safeGraphCall { networking.apolloClient.mutation(ResendVerificationEmailMutation(email)) }
  }
  
  override suspend fun verifyEmail(token: String): Result<IdpUser> {
    safeGraphCall { networking.apolloClient.mutation(VerifyEmailMutation(token)) }.onSuccess {
      return Result.success(
        it.verify_email?.userFragment?.toIdpUser() ?: IdpUser()
      )
    }.onFailure { return Result.failure(it) }
    return Result.failure(Throwable(UNKNOWN_ERROR))
  }
  
  override suspend fun forgotPassword(email: String): Result<ForgotPasswordMutation.Data> {
    return safeGraphCall { networking.apolloClient.mutation(ForgotPasswordMutation(email)) }
  }
  
  override suspend fun loginByEmailAndPassword(email: String, password: String): Result<LoginMutation.Data> {
    return safeGraphCall {
      networking.apolloClient.mutation(LoginMutation(LoginInputType(email, password)))
    }
  }
  
  override suspend fun resetForgottenPassword(
    token: String,
    password: String,
    confirmedPassword: String
  ): Result<ResetForgottenPasswordMutation.Data> {
    return safeGraphCall {
      networking.apolloClient.mutation(
        ResetForgottenPasswordMutation(
          ResetForgottenPasswordInputTypes
            (token, password, confirmedPassword)
        )
      )
    }
  }
  
  override suspend fun refreshTokens(): Result<RefreshTokenMutation.Data> {
    return safeGraphCall{
      networking.apolloClient.mutation(RefreshTokenMutation(encryptedPrefs.getRefreshTokenToken()))
    }
  }
  
  private suspend fun exchangeGoogleSdkAccessToken(googleTokenResponse: GoogleTokenResponse): Result<LoginResponse> {
    return safeApiCall {
      networking.api.exchangeTokens(
        provider = AuthProvider.GOOGLE.url, ExchangeTokenRequest(googleTokenResponse.access_token ?: "")
      )
    }
  }
}