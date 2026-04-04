package com.example.cricketbookingapp

import android.content.Context

object SessionManager {
    private const val PREF_NAME = "cricket_booking_session"
    private const val KEY_IS_SIGNED_IN = "is_signed_in"

    fun isSignedIn(context: Context): Boolean {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_IS_SIGNED_IN, false)
    }

    fun setSignedIn(context: Context, isSignedIn: Boolean) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_IS_SIGNED_IN, isSignedIn)
            .apply()
    }
}
