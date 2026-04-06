package com.example.cricketbookingapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddBookingActivity : AppCompatActivity() {
    private val dateTimeFormatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    private val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())

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

        val bookingNameInput = findViewById<TextInputEditText>(R.id.bookingNameInput)
        val customerNameInput = findViewById<TextInputEditText>(R.id.customerNameInput)
        val customerPhoneInput = findViewById<TextInputEditText>(R.id.customerPhoneInput)
        val startDateTimeInput = findViewById<TextInputEditText>(R.id.startDateTimeInput)
        val endDateTimeInput = findViewById<TextInputEditText>(R.id.endDateTimeInput)
        val pricePerHourInput = findViewById<TextInputEditText>(R.id.pricePerHourInput)
        val advancePaymentInput = findViewById<TextInputEditText>(R.id.advancePaymentInput)
        val discountInput = findViewById<TextInputEditText>(R.id.discountInput)
        val submitButton = findViewById<MaterialButton>(R.id.submitBookingButton)
        val addBookingLoader = findViewById<CircularProgressIndicator>(R.id.addBookingLoader)

        val bookingNameLayout = findViewById<TextInputLayout>(R.id.bookingNameLayout)
        val customerNameLayout = findViewById<TextInputLayout>(R.id.customerNameLayout)
        val customerPhoneLayout = findViewById<TextInputLayout>(R.id.customerPhoneLayout)
        val startDateTimeLayout = findViewById<TextInputLayout>(R.id.startDateTimeLayout)
        val endDateTimeLayout = findViewById<TextInputLayout>(R.id.endDateTimeLayout)
        val pricePerHourLayout = findViewById<TextInputLayout>(R.id.pricePerHourLayout)
        val advancePaymentLayout = findViewById<TextInputLayout>(R.id.advancePaymentLayout)
        val discountLayout = findViewById<TextInputLayout>(R.id.discountLayout)

        val summaryDateText = findViewById<TextView>(R.id.summaryDateText)
        val summaryCustomerText = findViewById<TextView>(R.id.summaryCustomerText)
        val summarySubtotalText = findViewById<TextView>(R.id.summarySubtotalText)
        val summaryAdvanceText = findViewById<TextView>(R.id.summaryAdvanceText)
        val summaryDiscountText = findViewById<TextView>(R.id.summaryDiscountText)
        val summaryTotalText = findViewById<TextView>(R.id.summaryTotalText)

        val updateSummary = {
            val startMillis = startDateTimeInput.tag as? Long
            val endMillis = endDateTimeInput.tag as? Long
            val customerName = customerNameInput.text?.toString()?.trim().orEmpty()
            val customerPhone = customerPhoneInput.text?.toString()?.trim().orEmpty()
            val pricePerHour = pricePerHourInput.text?.toString()?.trim().orEmpty().toDoubleOrNull() ?: 0.0
            val advancePayment = advancePaymentInput.text?.toString()?.trim().orEmpty().toDoubleOrNull() ?: 0.0
            val discount = discountInput.text?.toString()?.trim().orEmpty().toDoubleOrNull() ?: 0.0
            val totalHours = if (startMillis != null && endMillis != null && endMillis > startMillis) {
                calculateDurationHours(startMillis, endMillis)
            } else {
                0.0
            }
            val subTotal = pricePerHour * totalHours
            val finalTotal = (subTotal - discount).coerceAtLeast(0.0)

            summaryDateText.text = if (startMillis != null && endMillis != null) {
                "${dateFormatter.format(Date(startMillis))}\n${timeFormatter.format(Date(startMillis))} - ${timeFormatter.format(Date(endMillis))}"
            } else {
                getString(R.string.summary_empty_date)
            }
            summaryCustomerText.text = listOf(
                customerName.ifBlank { getString(R.string.summary_empty_customer) },
                customerPhone.ifBlank { getString(R.string.summary_empty_phone) }
            ).joinToString("\n")
            summarySubtotalText.text = getString(R.string.currency_value, formatAmount(subTotal))
            summaryAdvanceText.text = getString(R.string.currency_value, formatAmount(advancePayment))
            summaryDiscountText.text = getString(R.string.currency_value, formatAmount(discount))
            summaryTotalText.text = getString(R.string.currency_value, formatAmount(finalTotal))
        }

        configureDateTimeField(startDateTimeInput, updateSummary)
        configureDateTimeField(endDateTimeInput, updateSummary)

        listOf(
            bookingNameInput,
            customerNameInput,
            customerPhoneInput,
            pricePerHourInput,
            advancePaymentInput,
            discountInput
        ).forEach { input ->
            input.doAfterTextChanged { updateSummary() }
        }
        updateSummary()

        submitButton.setOnClickListener {
            listOf(
                bookingNameLayout,
                customerNameLayout,
                customerPhoneLayout,
                startDateTimeLayout,
                endDateTimeLayout,
                pricePerHourLayout,
                advancePaymentLayout,
                discountLayout
            ).forEach { it.error = null }

            val bookingName = bookingNameInput.text?.toString()?.trim().orEmpty()
            val customerName = customerNameInput.text?.toString()?.trim().orEmpty()
            val customerPhone = customerPhoneInput.text?.toString()?.trim().orEmpty()
            val pricePerHourText = pricePerHourInput.text?.toString()?.trim().orEmpty()
            val advancePaymentText = advancePaymentInput.text?.toString()?.trim().orEmpty().ifBlank { "0" }
            val discountText = discountInput.text?.toString()?.trim().orEmpty().ifBlank { "0" }
            val startMillis = startDateTimeInput.tag as? Long
            val endMillis = endDateTimeInput.tag as? Long

            when {
                bookingName.isBlank() -> {
                    bookingNameLayout.error = getString(R.string.booking_name_required)
                    bookingNameInput.requestFocus()
                    return@setOnClickListener
                }
                customerName.isBlank() -> {
                    customerNameLayout.error = getString(R.string.customer_name_required)
                    customerNameInput.requestFocus()
                    return@setOnClickListener
                }
                customerPhone.length != 10 || customerPhone.any { !it.isDigit() } -> {
                    customerPhoneLayout.error = getString(R.string.invalid_mobile_number)
                    customerPhoneInput.requestFocus()
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
                pricePerHourText.toDoubleOrNull() == null -> {
                    pricePerHourLayout.error = getString(R.string.invalid_booking_amount)
                    pricePerHourInput.requestFocus()
                    return@setOnClickListener
                }
                advancePaymentText.toDoubleOrNull() == null -> {
                    advancePaymentLayout.error = getString(R.string.invalid_booking_amount)
                    advancePaymentInput.requestFocus()
                    return@setOnClickListener
                }
                discountText.toDoubleOrNull() == null -> {
                    discountLayout.error = getString(R.string.invalid_booking_amount)
                    discountInput.requestFocus()
                    return@setOnClickListener
                }
                startMillis != null && endMillis != null && endMillis <= startMillis -> {
                    endDateTimeLayout.error = getString(R.string.end_must_be_after_start)
                    return@setOnClickListener
                }
            }

            val confirmedStartMillis = startMillis ?: return@setOnClickListener
            val confirmedEndMillis = endMillis ?: return@setOnClickListener
            val totalHours = calculateDurationHours(confirmedStartMillis, confirmedEndMillis)
            val finalAmount = ((pricePerHourText.toDoubleOrNull() ?: 0.0) * totalHours -
                (discountText.toDoubleOrNull() ?: 0.0)).coerceAtLeast(0.0)

            submitButton.isEnabled = false
            addBookingLoader.visibility = View.VISIBLE

            FirebaseRepository.addBooking(
                bookingName = bookingName,
                customerName = customerName,
                customerPhone = customerPhone,
                boxName = "Box 1",
                startDateTimeMillis = confirmedStartMillis,
                endDateTimeMillis = confirmedEndMillis,
                pricePerHour = pricePerHourText,
                advancePayment = advancePaymentText,
                discount = discountText,
                amount = formatAmount(finalAmount),
                onSuccess = {
                    submitButton.isEnabled = true
                    addBookingLoader.visibility = View.GONE
                    Toast.makeText(this, getString(R.string.booking_added_message), Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                },
                onError = { error ->
                    submitButton.isEnabled = true
                    addBookingLoader.visibility = View.GONE
                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun configureDateTimeField(input: TextInputEditText, onValueChanged: () -> Unit) {
        input.keyListener = null
        input.setOnClickListener {
            openDateThenTimePicker(input, onValueChanged)
        }
        input.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                openDateThenTimePicker(input, onValueChanged)
            }
        }
    }

    private fun openDateThenTimePicker(input: TextInputEditText, onValueChanged: () -> Unit) {
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
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)
                        input.setText(dateTimeFormatter.format(calendar.time))
                        input.tag = calendar.timeInMillis
                        onValueChanged()
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

    private fun formatAmount(value: Double): String {
        return if (value % 1.0 == 0.0) {
            value.toInt().toString()
        } else {
            String.format(Locale.getDefault(), "%.2f", value)
        }
    }

    private fun calculateDurationHours(startMillis: Long, endMillis: Long): Double {
        val durationMinutes = ((endMillis / 60000L) - (startMillis / 60000L)).coerceAtLeast(0L)
        return durationMinutes / 60.0
    }
}
