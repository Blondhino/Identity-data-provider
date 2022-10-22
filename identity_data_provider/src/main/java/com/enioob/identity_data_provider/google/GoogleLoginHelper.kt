package com.enioob.identity_data_provider

import com.enioob.identity_data_provider.google.GoogleLoginListener

interface GoogleLoginHelper {
  fun loginByGoogle()
  fun registerListener(listener : GoogleLoginListener)
  fun isUserAuthenticatedWithGoogle() : Boolean
  fun googleLogout()
}

