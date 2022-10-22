package com.enioob.identity_data_provider.repo

import com.enioob.identity_data_provider.ExchangeTokenRequest
import com.enioob.identity_data_provider.model.LoginResponse
import com.enioob.identity_data_provider.networking.NetworkingModule
import com.enioob.identity_data_provider.utils.AuthProvider
import com.enioob.identity_data_provider.utils.SafeApiCall

internal class IdpRepositoryImpl(val backendUrl : String) : IdpRepository {
  val networking = NetworkingModule(backendUrl)
  val safeApiCall = SafeApiCall()
  
  override suspend fun exchangeTokens(sdkToken: String, provider: AuthProvider): Result<LoginResponse> {
    return safeApiCall{
      networking.api.exchangeTokens(provider = provider.url, ExchangeTokenRequest(sdkToken))}
  }
}