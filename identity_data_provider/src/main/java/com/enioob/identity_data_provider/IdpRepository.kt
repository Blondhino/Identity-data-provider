package com.enioob.identity_data_provider

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.lifecycle.lifecycleScope
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
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
  private val callbackManager = CallbackManager.Factory.create()
  private val facebookLoginManager = LoginManager.getInstance()
  private lateinit var activity: ComponentActivity
  private lateinit var facebookLoginLauncher: ActivityResultLauncher<Collection<String>>
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
  
  override fun initializeFacebookLogin(componentActivity: ComponentActivity) {
    this.activity = componentActivity
    val contract: ActivityResultContract<Collection<String>, CallbackManager.ActivityResultParameters> =
      facebookLoginManager.createLogInActivityResultContract(callbackManager = callbackManager)
    componentActivity.lifecycleScope.launchWhenCreated {
      facebookLoginLauncher = componentActivity.registerForActivityResult(
        contract
      ) {}
    }
  }
  
  
  override fun loginWithFacebook() {
    onAuthProcessActivityChanged.invoke(true)
    if (!FacebookSdk.isInitialized()) {
      @Suppress("DEPRECATION")
      FacebookSdk.sdkInitialize(activity.application)
    }
    CoroutineScope(Dispatchers.IO).launch {
      val callback = object : FacebookCallback<LoginResult> {
        override fun onCancel() {
          Log.d("LOGIN_FB","canceled")
          onAuthProcessActivityChanged.invoke(false)
        }
        
        override fun onError(error: FacebookException) {
          Log.d("LOGIN_FB","error: "+error.message.toString())
          onAuthProcessActivityChanged.invoke(false)
        }
        
        override fun onSuccess(result: LoginResult) {
          val token = result.accessToken.token
          val userId = result.accessToken.userId
          Log.d("LOGIN_FB","succ")
          onAuthChangedListener.invoke(true)
          exchangeToken(token)
        }
      }
      
      facebookLoginManager.registerCallback(callbackManager,callback)
      facebookLoginLauncher.launch(emptyList())
    }
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
    if (AccessToken.getCurrentAccessToken() != null) {
      return true
    }
    return false
  }
  
  
  private fun exchangeToken(token: String) = CoroutineScope(Dispatchers.IO).launch {
    try {
      val resp = api.exchangeTokens("facebook", ExchangeTokenRequest(token))
      onAuthProcessActivityChanged.invoke(false)
      onAuthChangedListener.invoke(true)
    } catch (e: Exception) {
      Log.d("error:::", e.message.toString())
    }
  }
}
