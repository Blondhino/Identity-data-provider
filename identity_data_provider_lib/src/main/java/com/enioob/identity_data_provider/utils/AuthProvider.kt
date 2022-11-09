package com.enioob.identity_data_provider.utils

internal enum class AuthProvider (val url : String) {
  FACEBOOK("facebook"),
  GOOGLE("google"),
  APPLE("apple")
}