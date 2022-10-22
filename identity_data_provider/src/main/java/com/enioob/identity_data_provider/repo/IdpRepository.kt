package com.enioob.identity_data_provider.repo

import com.enioob.identity_data_provider.model.LoginResponse
import com.enioob.identity_data_provider.utils.AuthProvider

internal interface IdpRepository {
  suspend fun exchangeTokens(sdkToken : String, provider : AuthProvider) : Result<LoginResponse>
}