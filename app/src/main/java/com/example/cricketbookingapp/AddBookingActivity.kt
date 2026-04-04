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
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
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

        configureDateTimeField(startDateTimeInput)
        configureDateTimeField(endDateTimeInput)

        findViewById<MaterialButton>(R.id.submitBookingButton).setOnClickListener {
            Toast.makeText(this, getString(R.string.booking_submitted_message), Toast.LENGTH_SHORT).show()
            finish()
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
                        val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                        input.setText(formatter.format(calendar.time))
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
