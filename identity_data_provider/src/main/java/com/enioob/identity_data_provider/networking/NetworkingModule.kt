package com.enioob.identity_data_provider.networking

import android.util.Log
import com.apollographql.apollo3.ApolloClient
import com.enioob.identity_data_provider.GoogleApi
import com.enioob.identity_data_provider.TokenApi
import com.moczul.ok2curl.CurlInterceptor
import com.moczul.ok2curl.logger.Logger
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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
      GsonConverterFactory.create()
    ).build().create(
      TokenApi::class.java
    )
  }
  
  val googleApi by lazy {
    Retrofit.Builder().baseUrl("https://www.googleapis.com/").client(okHttp).addConverterFactory(
      GsonConverterFactory.create()
    ).build().create(
      GoogleApi::class.java
    )
  }
  
  val apolloClient by lazy {
    ApolloClient.Builder()
      .serverUrl("https://idp-7lx4w6qtva-ew.a.run.app/graphql")
      .build()
  }
  
}