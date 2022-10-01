package com.enioob.identity_data_provider

import android.util.Log
import com.moczul.ok2curl.CurlInterceptor
import com.moczul.ok2curl.logger.Logger
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

internal class NetworkingModule(backendBaseUrl: String) {
  
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
  val api by lazy {
    Retrofit.Builder().baseUrl(backendBaseUrl).client(okHttp).addConverterFactory(
      MoshiConverterFactory.create()
    ).build().create(
      TokenApi::class.java
    )
  }
  
  
}