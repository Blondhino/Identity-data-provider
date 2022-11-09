package com.enioob.identity_data_provider

import androidx.activity.ComponentActivity

internal fun getStringResourceByName(
  componentActivity: ComponentActivity,
  stringResourceName: String,
  fallbackString: String
): String {
  try {
    val packageName: String = componentActivity.packageName
    val resId: Int = componentActivity.resources.getIdentifier(stringResourceName, "string", packageName)
    return componentActivity.getString(resId)
  } catch (e: Exception) {
    return fallbackString
  }
}
