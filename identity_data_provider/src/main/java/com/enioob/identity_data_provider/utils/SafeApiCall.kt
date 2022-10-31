package com.enioob.identity_data_provider.utils

internal class SafeApiCall() {
  suspend operator fun <SuccessModel : Any> invoke(
    apiCall: suspend () -> SuccessModel
  ): Result<SuccessModel> {
    return try {
      Result.success(apiCall())
    } catch (throwable: Throwable) {
      Result.failure(throwable)
    }
  }
}