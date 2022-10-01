package com.enioob.identity_data_provider

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
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
  private lateinit var loginByGoogleContract: ActivityResultLauncher<Intent>
  private lateinit var googleSignInOptions: GoogleSignInOptions
  private lateinit var googleClient: GoogleSignInClient
  private lateinit var googleClientId: String
  private lateinit var facebookClientToken: String
  private lateinit var facebookLoginProtoclScheme: String
  private lateinit var facebookAppId : String
  
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
  
  override fun registerHostingActivity(componentActivity: ComponentActivity) {
    this.activity = componentActivity
    initializeResources()
    val contract: ActivityResultContract<Collection<String>, CallbackManager.ActivityResultParameters> =
      facebookLoginManager.createLogInActivityResultContract(callbackManager = callbackManager)
    componentActivity.lifecycleScope.launchWhenCreated {
      facebookLoginLauncher = componentActivity.registerForActivityResult(
        contract
      ) {}
    }
    
    loginByGoogleContract =
      componentActivity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result:
                                                                                                      ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
          val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(result.data)
          handleLoginByGoogleSdkResult(task);
        }
        onAuthProcessActivityChanged(false)
      }
    
    googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
      .requestEmail()
      .requestIdToken(googleClientId)
      .build()
    googleClient = GoogleSignIn.getClient(activity, googleSignInOptions);
    
  }
  
  private fun initializeResources() {
    googleClientId = getStringResourceByName("google_client_id", GOOGLE_CLIENT_ID_NOT_PROVIDED)
    facebookClientToken = getStringResourceByName("facebook_client_token", FACEBOOK_CLIENT_TOKEN_NOT_PROVIDED)
    facebookLoginProtoclScheme = getStringResourceByName("fb_login_protocol_scheme", FACEBOOK_LOGIN_PROTOCOL_SCHEME_NOT_PROVIDED)
    facebookAppId = getStringResourceByName("facebook_app_id", FACEBOOK_APP_ID_NOT_PROVIDED)
  }
  
  private fun handleLoginByGoogleSdkResult(completedTask: Task<GoogleSignInAccount>) {
    try {
      val account = completedTask.getResult(ApiException::class.java)
      Log.d("LOGIN_GOOGLE", "succ: " + account.idToken)
      account?.idToken?.let {
        onAuthChangedListener(true)
        exchangeToken(it, "google")
      }
    } catch (e: ApiException) {
      Log.d("LOGIN_GOOGLE", "signInResult:failed code=" + e.statusCode)
    }
  }
  
  override fun loginWithFacebook() {
    if (facebookAppId == FACEBOOK_APP_ID_NOT_PROVIDED) {
      throw IdpCredentialsException(
        "Looks like you are trying to use facebook login without providing facebook_app_id. " +
          "Read https://github.com/Blondhino/Identity-data-provider/blob/master/README for more info"
      )
    } else if (facebookLoginProtoclScheme == FACEBOOK_LOGIN_PROTOCOL_SCHEME_NOT_PROVIDED) {
      throw IdpCredentialsException(
        "Looks like you are trying to use facebook login without providing fb_login_protocol_scheme. " +
          "Read https://github.com/Blondhino/Identity-data-provider/blob/master/README for more info"
      )
    } else if (facebookClientToken == FACEBOOK_CLIENT_TOKEN_NOT_PROVIDED) {
      throw IdpCredentialsException(
        "Looks like you are trying to use facebook login without providing facebook_client_token. " +
          "Read https://github.com/Blondhino/Identity-data-provider/blob/master/README for more info"
      )
    }
    
    onAuthProcessActivityChanged.invoke(true)
    if (!FacebookSdk.isInitialized()) {
      @Suppress("DEPRECATION")
      FacebookSdk.sdkInitialize(activity.application)
    }
    CoroutineScope(Dispatchers.IO).launch {
      val callback = object : FacebookCallback<LoginResult> {
        override fun onCancel() {
          Log.d("LOGIN_FB", "canceled")
          onAuthProcessActivityChanged.invoke(false)
        }
        
        override fun onError(error: FacebookException) {
          Log.d("LOGIN_FB", "error: " + error.message.toString())
          onAuthProcessActivityChanged.invoke(false)
        }
        
        override fun onSuccess(result: LoginResult) {
          val token = result.accessToken.token
          val userId = result.accessToken.userId
          Log.d("LOGIN_FB", "succ")
          onAuthChangedListener.invoke(true)
          onAuthProcessActivityChanged.invoke(false)
          exchangeToken(token, "facebook")
        }
      }
      
      facebookLoginManager.registerCallback(callbackManager, callback)
      facebookLoginLauncher.launch(emptyList())
    }
  }
  
  override fun loginWithGoogle() {
    if (googleClientId == GOOGLE_CLIENT_ID_NOT_PROVIDED) {
      throw IdpCredentialsException(
        "Looks like you are trying to use google login without providing google_client_id. " +
          "Read https://github.com/Blondhino/Identity-data-provider/blob/master/README for more info"
      )
    }
    onAuthProcessActivityChanged(true)
    val signInIntent = googleClient.signInIntent
    loginByGoogleContract.launch(signInIntent)
    
  }
  
  override fun logout() {
    onAuthProcessActivityChanged(true)
    googleClient.signOut()
    LoginManager.getInstance().logOut()
    onAuthChangedListener.invoke(false)
    onAuthProcessActivityChanged(false)
  }
  
  override fun isUserAuthenticated(): Boolean {
    
    if (AccessToken.getCurrentAccessToken() != null || GoogleSignIn.getLastSignedInAccount(activity) != null) {
      return true
    }
    return false
  }
  
  
  private fun exchangeToken(token: String, provider: String) = CoroutineScope(Dispatchers.IO).launch {
    try {
      val resp = api.exchangeTokens(provider, ExchangeTokenRequest(token))
      onAuthProcessActivityChanged.invoke(false)
      onAuthChangedListener.invoke(true)
    } catch (e: Exception) {
      Log.d("error:::", e.message.toString())
    }
  }
  
  private fun getStringResourceByName(aString: String, fallbackString: String): String {
    try {
      val packageName: String = activity.packageName
      val resId: Int = activity.resources.getIdentifier(aString, "string", packageName)
      return activity.getString(resId)
    } catch (e: Exception) {
      Log.d("stringError", "called")
      return fallbackString
    }
  }
  
  private companion object {
    const val GOOGLE_CLIENT_ID_NOT_PROVIDED = "googleClientIdIsNotProvided"
    const val FACEBOOK_CLIENT_TOKEN_NOT_PROVIDED = "facebookClientTokenIsNotProvided"
    const val FACEBOOK_LOGIN_PROTOCOL_SCHEME_NOT_PROVIDED = "facebookLoginProtocolSchemeIsNotProvided"
    const val FACEBOOK_APP_ID_NOT_PROVIDED = "facebookAppIdIsNotProvided"
  }
}
