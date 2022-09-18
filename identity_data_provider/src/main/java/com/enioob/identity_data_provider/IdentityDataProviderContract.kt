package com.enioob.identity_data_provider

import android.app.Activity
import com.facebook.login.widget.LoginButton

internal interface IdentityDataProviderContract {
  fun loginWithFacebook(activity: Activity)
  fun loginWithGoogle()
  fun logout()
  fun isUserAuthenticated():Boolean
}