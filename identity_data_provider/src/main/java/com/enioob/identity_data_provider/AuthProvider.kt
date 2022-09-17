package com.enioob.identity_data_provider

internal enum class AuthProvider (val url : String) {
  FACEBOOK("facebook"),
  GOOGLE("google"),
  APPLE("apple")
}