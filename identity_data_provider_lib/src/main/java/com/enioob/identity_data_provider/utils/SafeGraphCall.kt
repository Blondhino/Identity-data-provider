package com.enioob.identity_data_provider.utils

import android.util.Log
import com.apollographql.apollo3.ApolloCall
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Executable
import com.apollographql.apollo3.api.Operation

internal class SafeGraphCall() {
  suspend operator fun <SuccessModel : Operation.Data> invoke(call: () -> ApolloCall<SuccessModel>): Result<SuccessModel> {
    try {
      val response = call().execute()
      if (response.hasErrors()) {
        val errors = response.errors
        val errorMessage = StringBuilder()
        errors?.forEach { errorMessage.append(it.message) }
        return Result.failure(Throwable(errorMessage.toString()))
      }
      response.data?.let {
        return Result.success(it)
      }
      return Result.failure(Throwable("Empty body"))
    } catch (throwable: Throwable) {
      return Result.failure(throwable)
    }
  }
  
  
}