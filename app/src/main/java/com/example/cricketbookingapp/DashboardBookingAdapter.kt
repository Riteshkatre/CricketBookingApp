package com.example.cricketbookingapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DashboardBookingAdapter(
    private var items: List<BookingItem>,
    private val onBookingClick: (BookingItem) -> Unit
) : RecyclerView.Adapter<DashboardBookingAdapter.DashboardBookingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DashboardBookingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dashboard_booking, parent, false)
        return DashboardBookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: DashboardBookingViewHolder, position: Int) {
        holder.bind(items[position], onBookingClick)
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(updatedItems: List<BookingItem>) {
        items = updatedItems
        notifyDataSetChanged()
    }

    class DashboardBookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val timeText: TextView = itemView.findViewById(R.id.dashboardBookingTimeText)
        private val titleText: TextView = itemView.findViewById(R.id.dashboardBookingTitleText)
        private val subtitleText: TextView = itemView.findViewById(R.id.dashboardBookingSubtitleText)

        fun bind(item: BookingItem, onBookingClick: (BookingItem) -> Unit) {
            timeText.text = item.timeRange
            titleText.text = item.name
            subtitleText.text = "${item.displayCustomerName} | ${item.displayCustomerPhone}"
            itemView.setOnClickListener { onBookingClick(item) }
        }
    }
}
