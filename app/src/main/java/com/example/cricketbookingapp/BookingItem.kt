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
    val createdByName: String = ""
) {
    val date: String
        get() = DATE_FORMAT.format(Date(startDateTimeMillis))

    val time: String
        get() = TIME_FORMAT.format(Date(startDateTimeMillis))

    companion object {
        private val DATE_FORMAT = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        private val TIME_FORMAT = SimpleDateFormat("hh:mm a", Locale.getDefault())
    }
}
