package com.enioob.identity_data_provider

import android.app.Activity
import androidx.activity.ComponentActivity
import com.facebook.login.widget.LoginButton

internal interface IdentityDataProviderContract {
  fun loginWithFacebook()
  fun loginWithGoogle()
  fun logout()
  fun isUserAuthenticated():Boolean
  fun initializeFacebookLogin(componentActivity: ComponentActivity)
}