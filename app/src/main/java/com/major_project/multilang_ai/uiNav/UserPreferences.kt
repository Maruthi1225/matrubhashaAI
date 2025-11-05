package com.major_project.multilang_ai.uiNav

import android.content.Context

object UserPreferences {
    private const val PREFS_NAME = "groot_prefs"
    private const val KEY_LANGUAGE = "preferred_language"
//    private const val KEY_TTS_RATE = "tts_rate"
//    private const val KEY_TTS_PITCH = "tts_pitch"
    private const val KEY_DARK_THEME = "dark_theme"
    private const val KEY_OFFLINE_TTS = "offline_tts"

    // ---------------------- LANGUAGE ----------------------
    fun setLanguage(context: Context, langCode: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, langCode).apply()
    }

    fun getLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANGUAGE, null) ?: "te-IN" // Default Telugu
    }

//    // ---------------------- TTS RATE ----------------------
//    fun setTtsRate(context: Context, rate: Float) {
//        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
//        prefs.edit().putFloat(KEY_TTS_RATE, rate).apply()
//    }
//
//    fun getTtsRate(context: Context): Float {
//        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
//        return prefs.getFloat(KEY_TTS_RATE, 1.0f) // Default = normal speed
//    }
//
//    // ---------------------- TTS PITCH ----------------------
//    fun setTtsPitch(context: Context, pitch: Float) {
//        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
//        prefs.edit().putFloat(KEY_TTS_PITCH, pitch).apply()
//    }
//
//    fun getTtsPitch(context: Context): Float {
//        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
//        return prefs.getFloat(KEY_TTS_PITCH, 1.0f) // Default = normal pitch
//    }

    // ---------------------- DARK THEME ----------------------
    fun setDarkTheme(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_DARK_THEME, enabled).apply()
    }


    fun isDarkTheme(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_DARK_THEME, true)
    }

    // ---------------------- OFFLINE TTS ----------------------
    fun setOfflineTts(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_OFFLINE_TTS, enabled).apply()
    }

    fun isOfflineTts(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_OFFLINE_TTS, false)
    }

    // ---------------------- RESET ALL ----------------------
    fun resetAll(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}