package com.major_project.multilang_ai

import android.content.Context

object UserPreferences {
    private const val PREFS_NAME = "groot_prefs"
    private const val KEY_LANGUAGE = "preferred_language"

    fun setLanguage(context: Context, langCode: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, langCode).apply()
    }

    fun getLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        // use Elvis operator to handle null safely
        return prefs.getString(KEY_LANGUAGE, null) ?: "te-IN" // default Hindi
    }
}
