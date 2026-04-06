package com.example.cricketbookingapp

import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

fun AppCompatActivity.allowScreenSharing() {
    window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
}
