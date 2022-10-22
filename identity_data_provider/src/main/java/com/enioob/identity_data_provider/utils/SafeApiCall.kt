package com.enioob.identity_data_provider.utils

class SafeApiCall() {
  suspend operator fun <SuccessModel : Any> invoke(
    apiCall: suspend () -> Result<SuccessModel>
  ): Result<SuccessModel> {
    return try {
      val response = apiCall()
      response.onFailure { return Result.failure(it) }
      response
    } catch (exception: Exception) {
      return Result.failure(exception)
    }
  }
}