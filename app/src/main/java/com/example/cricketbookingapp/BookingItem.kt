package com.example.cricketbookingapp

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BookingItem(
    val id: String = "",
    val name: String = "",
    val startDateTimeMillis: Long = 0L,
    val endDateTimeMillis: Long = 0L,
    val amount: String = "",
    val createdByName: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val pricePerHour: String = "",
    val advancePayment: String = "",
    val discount: String = "",
    val boxName: String = "Box 1"
) {
    val date: String
        get() = DATE_FORMAT.format(Date(startDateTimeMillis))

    val time: String
        get() = TIME_FORMAT.format(Date(startDateTimeMillis))

    val endTime: String
        get() = TIME_FORMAT.format(Date(endDateTimeMillis))

    val timeRange: String
        get() = "${TIME_FORMAT.format(Date(startDateTimeMillis))} - ${TIME_FORMAT.format(Date(endDateTimeMillis))}"

    val durationHours: Double
        get() = ((endDateTimeMillis - startDateTimeMillis).coerceAtLeast(0L)) / (60.0 * 60.0 * 1000.0)

    val displayAmount: String
        get() = "Rs. ${amount.ifBlank { "0" }}"

    val displayCustomerName: String
        get() = customerName.ifBlank { name }

    val displayCustomerPhone: String
        get() = customerPhone.ifBlank { "Not provided" }

    companion object {
        private val DATE_FORMAT = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        private val TIME_FORMAT = SimpleDateFormat("hh:mm a", Locale.getDefault())
    }
}
