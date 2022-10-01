package com.enioob.identity_data_provider

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import java.lang.Exception

class GoogleLoginHelperImpl(val componentActivity: ComponentActivity) : GoogleLoginHelper {
  private var loginByGoogleContract: ActivityResultLauncher<Intent>
  private var googleSignInOptions: GoogleSignInOptions
  private var googleClient: GoogleSignInClient
  private lateinit var googleClientId: String
  private lateinit var listener : GoogleLoginListener

  
  override fun registerListener(listener : GoogleLoginListener){
    this.listener = listener
  }
  
  init {
    googleClientId = getStringResourceByName("google_client_id", GOOGLE_CLIENT_ID_NOT_PROVIDED)
    loginByGoogleContract =
      componentActivity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result:
                                                                                                      ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
          val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(result.data)
          handleLoginByGoogleSdkResult(task);
        }
      }
  
    googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
      .requestEmail()
      .requestIdToken(googleClientId)
      .build()
    googleClient = GoogleSignIn.getClient(componentActivity, googleSignInOptions);
    
  }
  
  override fun loginByGoogle() {
    if(googleClientId == GOOGLE_CLIENT_ID_NOT_PROVIDED){
      throw IdpCredentialsException("Looks like you are trying to use google login without providing google_client_id. Read " +
        "https://github.com/Blondhino/Identity-data-provider/blob/master/README.md for more info")
    }
    
    try {
    listener.onLoginProcessStart()
    }catch (e:Exception){}
    
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
      account?.idToken?.let {
        try {
          listener.onLoginProcessEnd()
          listener.onSuccess(it)
        }catch (e:Exception){}
      }
    } catch (e: ApiException) {
      Log.d("LOGIN_GOOGLE", "signInResult:failed code=" + e.statusCode)
      try {
        listener.onError(e.statusCode.toString())
      }catch (e:Exception){}
    }
  }
  
  private fun getStringResourceByName(aString: String, fallbackString: String): String {
    try {
      val packageName: String = componentActivity.packageName
      val resId: Int = componentActivity.resources.getIdentifier(aString, "string", packageName)
      return componentActivity.getString(resId)
    } catch (e: Exception) {
      return fallbackString
    }
  }
  
  private companion object{
    const val GOOGLE_CLIENT_ID_NOT_PROVIDED = "googleClientIdIsNotProvided"
  }
  
}