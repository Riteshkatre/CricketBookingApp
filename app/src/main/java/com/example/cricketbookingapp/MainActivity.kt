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
    private lateinit var loadingDialog: BrandedLoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        loadingDialog = BrandedLoadingDialog(this)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val tabLayout = findViewById<TabLayout>(R.id.authTabs)
        val signInContainer = findViewById<View>(R.id.signInContainer)
        val signUpContainer = findViewById<View>(R.id.signUpContainer)
        val emailInput = findViewById<TextInputEditText>(R.id.signInEmailInput)
        val passwordInput = findViewById<TextInputEditText>(R.id.signInPasswordInput)
        val submitButton = findViewById<MaterialButton>(R.id.signInSubmitButton)
        val signUpFirstNameInput = findViewById<TextInputEditText>(R.id.signUpFirstNameInput)
        val signUpLastNameInput = findViewById<TextInputEditText>(R.id.signUpLastNameInput)
        val signUpMobileInput = findViewById<TextInputEditText>(R.id.signUpMobileInput)
        val signUpEmailInput = findViewById<TextInputEditText>(R.id.signUpEmailInput)
        val signUpPasswordInput = findViewById<TextInputEditText>(R.id.signUpPasswordInput)
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
            val email = emailInput.text?.toString()?.trim()
            val password = passwordInput.text?.toString()?.trim()

            if (email.isNullOrBlank() || password.isNullOrBlank()) {
                Toast.makeText(this, getString(R.string.enter_sign_in_email_password), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, getString(R.string.invalid_email), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            submitButton.isEnabled = false
            loadingDialog.show()
            FirebaseRepository.signInWithEmail(
                email = email,
                password = password,
                onSuccess = {
                    submitButton.isEnabled = true
                    loadingDialog.hide()
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                },
                onError = { error ->
                    submitButton.isEnabled = true
                    loadingDialog.hide()
                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                }
            )
        }

        saveButton.setOnClickListener {
            val firstName = signUpFirstNameInput.text?.toString()?.trim().orEmpty()
            val lastName = signUpLastNameInput.text?.toString()?.trim().orEmpty()
            val mobile = signUpMobileInput.text?.toString()?.trim().orEmpty()
            val email = signUpEmailInput.text?.toString()?.trim().orEmpty()
            val password = signUpPasswordInput.text?.toString()?.trim().orEmpty()

            when {
                firstName.isBlank() || lastName.isBlank() || mobile.isBlank() || email.isBlank() || password.isBlank() -> {
                    Toast.makeText(this, getString(R.string.enter_all_sign_up_fields), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                mobile.length != 10 -> {
                    Toast.makeText(this, getString(R.string.invalid_mobile_number), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    Toast.makeText(this, getString(R.string.invalid_email), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                password.length < 6 -> {
                    Toast.makeText(this, getString(R.string.password_min_length), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            saveButton.isEnabled = false
            loadingDialog.show()
            FirebaseRepository.signUp(
                firstName = firstName,
                lastName = lastName,
                mobileNumber = mobile,
                email = email,
                password = password,
                onSuccess = {
                    saveButton.isEnabled = true
                    loadingDialog.hide()
                    Toast.makeText(this, getString(R.string.sign_up_saved_message), Toast.LENGTH_SHORT).show()
                    signUpFirstNameInput.text = null
                    signUpLastNameInput.text = null
                    signUpMobileInput.text = null
                    signUpEmailInput.text = null
                    signUpPasswordInput.text = null
                    tabLayout.getTabAt(0)?.select()
                },
                onError = { error ->
                    saveButton.isEnabled = true
                    loadingDialog.hide()
                    val message = when (error) {
                        "Mobile number already registered" -> getString(R.string.mobile_already_registered)
                        else -> error
                    }
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    override fun onDestroy() {
        loadingDialog.hide()
        super.onDestroy()
    }
}
