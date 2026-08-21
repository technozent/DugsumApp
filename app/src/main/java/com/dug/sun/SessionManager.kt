package com.dug.sun

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "dugsum_prefs"
        private const val IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USERNAME = "username"
        private const val KEY_PLAN_START = "plan_start"
        private const val KEY_PLAN_END = "plan_end"
        private const val KEY_AUTH_TOKEN = "auth_token"
    }

    fun setLoggedIn(loggedIn: Boolean) {
        prefs.edit().putBoolean(IS_LOGGED_IN, loggedIn).apply()
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(IS_LOGGED_IN, false)
    }

    fun saveUserDetails(username: String?, planStart: String?, planEnd: String?, token: String? = null) {
        prefs.edit().apply {
            putString(KEY_USERNAME, username)
            putString(KEY_PLAN_START, planStart)
            putString(KEY_PLAN_END, planEnd)
            token?.let { putString(KEY_AUTH_TOKEN, it) }
            apply()
        }
    }

    fun getUsername(): String? = prefs.getString(KEY_USERNAME, null)
    fun getPlanStart(): String? = prefs.getString(KEY_PLAN_START, null)
    fun getPlanEnd(): String? = prefs.getString(KEY_PLAN_END, null)
    fun getAuthToken(): String? = prefs.getString(KEY_AUTH_TOKEN, null)

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}