package com.example.cricketbookingapp

import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity

class BrandedLoadingDialog(
    private val activity: AppCompatActivity
) {
    private val dialog: Dialog by lazy {
        Dialog(activity).apply {
            setContentView(R.layout.dialog_branded_loading)
            setCancelable(false)
            window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
        }
    }

    fun show(@StringRes messageResId: Int = R.string.loading_message) {
        if (activity.isFinishing || activity.isDestroyed) return
        dialog.findViewById<TextView>(R.id.loadingMessageText)?.setText(messageResId)
        if (!dialog.isShowing) {
            dialog.show()
        }
    }

    fun hide() {
        if (dialog.isShowing) {
            dialog.dismiss()
        }
    }
}
