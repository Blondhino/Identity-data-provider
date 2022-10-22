package com.enioob.identity_data_provider.google

import android.content.Intent
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.enioob.identity_data_provider.GoogleLoginHelper
import com.enioob.identity_data_provider.utils.IdpCredentialsException
import com.enioob.identity_data_provider.getStringResourceByName
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task


class GoogleLoginHelperImpl(val componentActivity: ComponentActivity) : GoogleLoginHelper {
  private var loginByGoogleContract: ActivityResultLauncher<Intent>
  private var googleSignInOptions: GoogleSignInOptions
  private var googleClient: GoogleSignInClient
  private var googleClientId: String =
    getStringResourceByName(componentActivity, "google_client_id", GOOGLE_CLIENT_ID_NOT_PROVIDED)
  private lateinit var listener: GoogleLoginListener
  
  
  override fun registerListener(listener: GoogleLoginListener) {
    this.listener = listener
  }
  
  init {
    loginByGoogleContract =
      componentActivity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result:
                                                                                                      ActivityResult ->
        val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        handleLoginByGoogleSdkResult(task);
      }
    
    googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
      .requestEmail()
      .requestIdToken(googleClientId)
      .requestServerAuthCode(googleClientId, true)
      .build()
    googleClient = GoogleSignIn.getClient(componentActivity, googleSignInOptions);
    
  }
  
  override fun loginByGoogle() {
    if (googleClientId == GOOGLE_CLIENT_ID_NOT_PROVIDED) {
      throw IdpCredentialsException(
        "Looks like you are trying to use google login without providing google_client_id. Read " +
          "https://github.com/Blondhino/Identity-data-provider/blob/master/README.md for more info"
      )
    }
    
    try {
      listener.onLoginProcessStart()
    } catch (e: Exception) {
      Log.d("LOGIN_GOOGLE", e.message.toString())
    }
    
    val signInIntent = googleClient.signInIntent
    loginByGoogleContract.launch(signInIntent)
  }
  
  
  override fun isUserAuthenticatedWithGoogle(): Boolean {
    return GoogleSignIn.getLastSignedInAccount(componentActivity) != null
  }
  
  override fun googleLogout() {
    googleClient.signOut()
  }
  
  private fun handleLoginByGoogleSdkResult(completedTask: Task<GoogleSignInAccount>) {
    try {
      val account = completedTask.getResult(ApiException::class.java)
      Log.d("LOGIN_GOOGLE", "succ: " + account.idToken)
      account?.serverAuthCode?.let {
        try {
          exchangeAuthCodeForToken(it)
        } catch (e: Exception) {
          Log.d("LOGIN_GOOGLE", e.message.toString())
        }
      }
    } catch (e: ApiException) {
      Log.d("LOGIN_GOOGLE", "signInResult:failed code=" + e.statusCode)
      try {
        listener.onError(e.statusCode.toString())
      } catch (e: Exception) {
        Log.d("GOOGLE_ERROR", e.message.toString())
      }
    }
  }
  
  private fun exchangeAuthCodeForToken(token: String) {
    listener.onSuccess(token)
  }
  
  private companion object {
    const val GOOGLE_CLIENT_ID_NOT_PROVIDED = "googleClientIdIsNotProvided"
  }
  
}