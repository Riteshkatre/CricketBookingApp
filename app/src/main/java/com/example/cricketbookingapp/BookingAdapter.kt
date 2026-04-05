package com.example.cricketbookingapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BookingAdapter(
    private var bookings: List<BookingItem>,
    private val onBookingClick: (BookingItem) -> Unit
) : RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_booking, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        holder.bind(bookings[position], onBookingClick)
    }

    override fun getItemCount(): Int = bookings.size

    fun updateBookings(updatedBookings: List<BookingItem>) {
        bookings = updatedBookings
        notifyDataSetChanged()
    }

    class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val bookingDay: TextView = itemView.findViewById(R.id.bookingDayText)
        private val bookingMonth: TextView = itemView.findViewById(R.id.bookingMonthText)
        private val bookingTime: TextView = itemView.findViewById(R.id.bookingTimeText)
        private val bookingName: TextView = itemView.findViewById(R.id.bookingNameText)
        private val bookingCustomer: TextView = itemView.findViewById(R.id.bookingCustomerText)
        private val bookingPhone: TextView = itemView.findViewById(R.id.bookingPhoneText)
        private val bookingAmount: TextView = itemView.findViewById(R.id.bookingAmountText)

        fun bind(item: BookingItem, onBookingClick: (BookingItem) -> Unit) {
            val dateParts = item.date.split(" ")
            bookingDay.text = dateParts.firstOrNull().orEmpty()
            bookingMonth.text = dateParts.drop(1).joinToString(" ")
            bookingTime.text = item.timeRange
            bookingName.text = item.name
            bookingCustomer.text = item.displayCustomerName
            bookingPhone.text = item.displayCustomerPhone
            bookingAmount.text = item.displayAmount
            itemView.setOnClickListener { onBookingClick(item) }
        }
    }
}
