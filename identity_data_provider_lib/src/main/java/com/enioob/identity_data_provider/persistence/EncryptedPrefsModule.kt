package com.enioob.identity_data_provider.persistence

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys


internal class EncryptedPrefsModule(context: Context) {
  private val sharedPreferences by lazy {
    val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    EncryptedSharedPreferences.create(
      "secret_shared_prefs",
      masterKeyAlias,
      context,
      EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
      EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
  }
  
  
  fun saveToken(token: String) {
    sharedPreferences.edit().putString(ENCRYPTED_TOKEN, token).apply()
  }
  
  fun saveRefreshToken(refreshToken: String) {
    sharedPreferences.edit().putString(ENCRYPTED_REFRESH_TOKEN, refreshToken).apply()
  }
  
  fun getToken(): String {
    return sharedPreferences.getString(ENCRYPTED_TOKEN, null) ?: ""
  }
  
  fun getRefreshToken(): String {
    return sharedPreferences.getString(ENCRYPTED_REFRESH_TOKEN, null) ?: ""
  }
  
  private companion object {
    const val ENCRYPTED_TOKEN = "encryptedToken"
    const val ENCRYPTED_REFRESH_TOKEN = "encryptedRefreshToken"
  }
}