package com.example.cricketbookingapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.tabs.TabLayout

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val tabLayout = findViewById<TabLayout>(R.id.authTabs)
        val signInContainer = findViewById<View>(R.id.signInContainer)
        val signUpContainer = findViewById<View>(R.id.signUpContainer)
        val mobileInput = findViewById<TextInputEditText>(R.id.signInMobileInput)
        val passwordInput = findViewById<TextInputEditText>(R.id.signInPasswordInput)
        val submitButton = findViewById<MaterialButton>(R.id.signInSubmitButton)
        val saveButton = findViewById<MaterialButton>(R.id.signUpSaveButton)

        fun showTab(position: Int) {
            signInContainer.visibility = if (position == 0) View.VISIBLE else View.GONE
            signUpContainer.visibility = if (position == 1) View.VISIBLE else View.GONE
        }

        showTab(0)
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                showTab(tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) = Unit

            override fun onTabReselected(tab: TabLayout.Tab) = Unit
        })

        submitButton.setOnClickListener {
            val mobile = mobileInput.text?.toString()?.trim()
            val password = passwordInput.text?.toString()?.trim()

            if (mobile == "6265784954" && password == "1234") {
                SessionManager.setSignedIn(this, true)
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, getString(R.string.invalid_sign_in_message), Toast.LENGTH_SHORT).show()
            }
        }

        saveButton.setOnClickListener {
            Toast.makeText(this, getString(R.string.sign_up_saved_message), Toast.LENGTH_SHORT).show()
        }
    }
}
