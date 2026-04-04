package com.example.cricketbookingapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BookingAdapter(
    private var bookings: List<BookingItem>
) : RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_booking, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        holder.bind(bookings[position])
    }

    override fun getItemCount(): Int = bookings.size

    fun updateBookings(updatedBookings: List<BookingItem>) {
        bookings = updatedBookings
        notifyDataSetChanged()
    }

    class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val bookingName: TextView = itemView.findViewById(R.id.bookingNameText)
        private val bookingDate: TextView = itemView.findViewById(R.id.bookingDateText)
        private val bookingTime: TextView = itemView.findViewById(R.id.bookingTimeText)
        private val bookingAmount: TextView = itemView.findViewById(R.id.bookingAmountText)

        fun bind(item: BookingItem) {
            bookingName.text = item.name
            bookingDate.text = item.date
            bookingTime.text = item.time
            bookingAmount.text = item.amount
        }
    }
}
