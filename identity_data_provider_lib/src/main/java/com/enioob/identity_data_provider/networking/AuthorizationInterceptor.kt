package com.enioob.identity_data_provider.networking

import android.content.Context
import android.util.Log
import com.enioob.identity_data_provider.persistence.EncryptedPrefsModule
import okhttp3.Interceptor
import okhttp3.Response

internal class AuthorizationInterceptor(val context: Context): Interceptor {
  private val encryptedPrefs = EncryptedPrefsModule(context)
  override fun intercept(chain: Interceptor.Chain): Response {
    val token = encryptedPrefs.getToken()
    val request = chain.request().newBuilder()
      if(token.isNotEmpty()){
        request.addHeader("Authorization", "Bearer $token")
      }
    return chain.proceed(request.build())
  }
}