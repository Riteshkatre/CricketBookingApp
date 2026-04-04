package com.example.cricketbookingapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.Calendar
import java.util.Locale

class AddBookingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_booking)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.addBookingRoot)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<MaterialToolbar>(R.id.addBookingToolbar).setNavigationOnClickListener {
            finish()
        }

        val startDateTimeInput = findViewById<TextInputEditText>(R.id.startDateTimeInput)
        val endDateTimeInput = findViewById<TextInputEditText>(R.id.endDateTimeInput)
        val bookingNameInput = findViewById<TextInputEditText>(R.id.bookingNameInput)
        val amountInput = findViewById<TextInputEditText>(R.id.amountInput)
        val submitButton = findViewById<MaterialButton>(R.id.submitBookingButton)
        val addBookingLoader = findViewById<CircularProgressIndicator>(R.id.addBookingLoader)
        val bookingNameLayout = findViewById<TextInputLayout>(R.id.bookingNameLayout)
        val startDateTimeLayout = findViewById<TextInputLayout>(R.id.startDateTimeLayout)
        val endDateTimeLayout = findViewById<TextInputLayout>(R.id.endDateTimeLayout)
        val amountLayout = findViewById<TextInputLayout>(R.id.amountLayout)

        configureDateTimeField(startDateTimeInput)
        configureDateTimeField(endDateTimeInput)

        submitButton.setOnClickListener {
            bookingNameLayout.error = null
            startDateTimeLayout.error = null
            endDateTimeLayout.error = null
            amountLayout.error = null

            val bookingName = bookingNameInput.text?.toString()?.trim().orEmpty()
            val amount = amountInput.text?.toString()?.trim().orEmpty()
            val startMillis = startDateTimeInput.tag as? Long
            val endMillis = endDateTimeInput.tag as? Long

            when {
                bookingName.isBlank() -> {
                    bookingNameLayout.error = getString(R.string.booking_name_required)
                    bookingNameInput.requestFocus()
                    return@setOnClickListener
                }
                startMillis == null -> {
                    startDateTimeLayout.error = getString(R.string.start_date_required)
                    startDateTimeInput.requestFocus()
                    return@setOnClickListener
                }
                endMillis == null -> {
                    endDateTimeLayout.error = getString(R.string.end_date_required)
                    endDateTimeInput.requestFocus()
                    return@setOnClickListener
                }
                amount.isBlank() -> {
                    amountLayout.error = getString(R.string.amount_required)
                    amountInput.requestFocus()
                    return@setOnClickListener
                }
                amount.toDoubleOrNull() == null -> {
                    amountLayout.error = getString(R.string.invalid_booking_amount)
                    amountInput.requestFocus()
                    return@setOnClickListener
                }
                endMillis <= startMillis -> {
                    endDateTimeLayout.error = getString(R.string.end_must_be_after_start)
                    return@setOnClickListener
                }
            }

            submitButton.isEnabled = false
            addBookingLoader.visibility = android.view.View.VISIBLE
            FirebaseRepository.addBooking(
                bookingName = bookingName,
                startDateTimeMillis = startMillis,
                endDateTimeMillis = endMillis,
                amount = amount,
                onSuccess = {
                    submitButton.isEnabled = true
                    addBookingLoader.visibility = android.view.View.GONE
                    Toast.makeText(this, getString(R.string.booking_added_message), Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                },
                onError = { error ->
                    submitButton.isEnabled = true
                    addBookingLoader.visibility = android.view.View.GONE
                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun configureDateTimeField(input: TextInputEditText) {
        input.keyListener = null
        input.setOnClickListener {
            openDateThenTimePicker(input)
        }
        input.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                openDateThenTimePicker(input)
            }
        }
    }

    private fun openDateThenTimePicker(input: TextInputEditText) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                TimePickerDialog(
                    this,
                    { _, hourOfDay, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)
                        val formatted = java.text.SimpleDateFormat(
                            "dd MMM yyyy, hh:mm a",
                            Locale.getDefault()
                        ).format(calendar.time)
                        input.setText(formatted)
                        input.tag = calendar.timeInMillis
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}
