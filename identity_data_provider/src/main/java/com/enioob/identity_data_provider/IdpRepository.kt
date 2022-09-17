package com.enioob.identity_data_provider

import android.util.Log
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.moczul.ok2curl.CurlInterceptor
import com.moczul.ok2curl.logger.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

internal class IdpRepository(val backendBaseUrl: String) : IdentityDataProviderContract {
  
  var onAuthChangedListener: ((isAuthenticated: Boolean) -> Unit) = {}
  var onAuthProcessActivityChanged: ((isAuthProcessActive: Boolean) -> Unit) = {}
  
  private val loggingInterceptor by lazy { HttpLoggingInterceptor() }.apply {
    this.value.level = HttpLoggingInterceptor.Level.BODY
  }
  private val curlInterceptor by lazy {
    CurlInterceptor(object : Logger {
      override fun log(message: String) {
        Log.d("CURL:::", message)
      }
    })
  }
  private val okHttp by lazy {
    OkHttpClient().newBuilder().addInterceptor(curlInterceptor).addInterceptor(loggingInterceptor).build()
  }
  private val api by lazy {
    Retrofit.Builder().baseUrl(backendBaseUrl).client(okHttp).addConverterFactory(
      MoshiConverterFactory.create()
    ).build().create(
      TokenApi::class.java
    )
  }
  
  
  override fun loginWithFacebook(facebookLoginButton: LoginButton) {
    onAuthProcessActivityChanged(true)
    facebookLoginButton.registerCallback(CallbackManager.Factory.create(),object : FacebookCallback<LoginResult> {
      override fun onCancel() {
        onAuthProcessActivityChanged(false)
      }
    
      override fun onError(error: FacebookException) {
        onAuthProcessActivityChanged(false)
      }
    
      override fun onSuccess(result: LoginResult) {
        onAuthChangedListener.invoke(true)
        onAuthProcessActivityChanged(false)
        exchangeToken(result.accessToken.token)
      }
    
    })
  }
  
  override fun loginWithGoogle() {
  
  }
  
  override fun logout() {
    onAuthProcessActivityChanged(true)
    LoginManager.getInstance().logOut()
    onAuthChangedListener.invoke(false)
    onAuthProcessActivityChanged(false)
  }
  
  override fun isUserAuthenticated(): Boolean {
    if(AccessToken.getCurrentAccessToken()!=null){
      return true
    }
    return false
  }
  
  
  private fun exchangeToken(token: String) = CoroutineScope(Dispatchers.IO).launch {
    try {
      val resp =  api.exchangeTokens("facebook", ExchangeTokenRequest(token))
    }catch (e:Exception){
      Log.d("error:::",e.message.toString())
    }
  }
}