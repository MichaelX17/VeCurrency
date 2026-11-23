package com.example.vecurrency.data.local

import android.content.Context
import android.content.SharedPreferences

class SettingsPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("vecurrency_settings", Context.MODE_PRIVATE)

    var isDarkTheme: Boolean
        get() = prefs.getBoolean("is_dark_theme", true)
        set(value) = prefs.edit().putBoolean("is_dark_theme", value).apply()

    var language: String
        get() = prefs.getString("language", "ES") ?: "ES"
        set(value) = prefs.edit().putString("language", value).apply()
}
