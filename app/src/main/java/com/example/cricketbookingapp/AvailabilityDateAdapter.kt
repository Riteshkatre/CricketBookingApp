package com.example.cricketbookingapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AvailabilityDateAdapter(
    private var items: List<Long>,
    private val onDateSelected: (Long) -> Unit
) : RecyclerView.Adapter<AvailabilityDateAdapter.DateViewHolder>() {

    private var selectedDateMillis: Long = items.firstOrNull() ?: 0L

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_availability_date, parent, false)
        return DateViewHolder(view)
    }

    override fun onBindViewHolder(holder: DateViewHolder, position: Int) {
        holder.bind(items[position], items[position] == selectedDateMillis, onDateSelected = { date ->
            selectedDateMillis = date
            notifyDataSetChanged()
            onDateSelected(date)
        })
    }

    override fun getItemCount(): Int = items.size

    fun updateDates(updatedItems: List<Long>, selectedDate: Long) {
        items = updatedItems
        selectedDateMillis = selectedDate
        notifyDataSetChanged()
    }

    class DateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val monthText: TextView = itemView.findViewById(R.id.dateMonthText)
        private val dayText: TextView = itemView.findViewById(R.id.dateDayText)
        private val weekText: TextView = itemView.findViewById(R.id.dateWeekText)

        fun bind(dateMillis: Long, isSelected: Boolean, onDateSelected: (Long) -> Unit) {
            val date = Date(dateMillis)
            monthText.text = MONTH_FORMAT.format(date)
            dayText.text = DAY_FORMAT.format(date)
            weekText.text = WEEK_FORMAT.format(date)

            itemView.isSelected = isSelected
            itemView.setBackgroundResource(
                if (isSelected) R.drawable.bg_date_selected else R.drawable.bg_date_unselected
            )
            itemView.setOnClickListener { onDateSelected(dateMillis) }
        }
    }

    companion object {
        private val MONTH_FORMAT = SimpleDateFormat("MMM", Locale.getDefault())
        private val DAY_FORMAT = SimpleDateFormat("dd", Locale.getDefault())
        private val WEEK_FORMAT = SimpleDateFormat("EEE", Locale.getDefault())
    }
}
