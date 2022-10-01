package com.enioob.identity_data_provider

import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FacebookLoginHelperImpl(private val componentActivity: ComponentActivity) : FacebookLoginHelper {
  private val callbackManager = CallbackManager.Factory.create()
  private val facebookLoginManager = LoginManager.getInstance()
  private lateinit var facebookLoginLauncher: ActivityResultLauncher<Collection<String>>
  private var contract: ActivityResultContract<Collection<String>, CallbackManager.ActivityResultParameters> =
    facebookLoginManager.createLogInActivityResultContract(callbackManager = callbackManager)
  
  init {
    componentActivity.lifecycleScope.launchWhenCreated {
      facebookLoginLauncher = componentActivity.registerForActivityResult(
        contract
      ) {}
    }
    
  }

  
  
  override fun loginByFacebook(
    onProcessStart: () -> Unit,
    onProcessEnd: () -> Unit,
    onSuccess: (sdkToken: String) -> Unit,
    onError: (error: String) -> Unit
  ) {
  
    
    onProcessStart()
    if (!FacebookSdk.isInitialized()) {
      @Suppress("DEPRECATION")
      FacebookSdk.sdkInitialize(componentActivity.application)
    }
    CoroutineScope(Dispatchers.IO).launch {
      val callback = object : FacebookCallback<LoginResult> {
        override fun onCancel() {
          Log.d("LOGIN_FB", "canceled")
          onProcessEnd()
        }
      
        override fun onError(error: FacebookException) {
          Log.d("LOGIN_FB", "error: " + error.message.toString())
          onProcessEnd()
          onError(error.message.toString())
        }
      
        override fun onSuccess(result: LoginResult) {
          val token = result.accessToken.token
          val userId = result.accessToken.userId
          Log.d("LOGIN_FB", "succ")
          onProcessEnd()
          onSuccess(token)
        }
      }
    
      facebookLoginManager.registerCallback(callbackManager, callback)
      facebookLoginLauncher.launch(emptyList())
    }
    
  }
  
  override fun isUserAuthenticatedWithFacebook(): Boolean {
    return AccessToken.getCurrentAccessToken() != null
  }
  
  override fun facebookLogout() {
    LoginManager.getInstance().logOut()
  }
  
}