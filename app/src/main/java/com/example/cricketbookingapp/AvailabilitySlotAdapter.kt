package com.example.cricketbookingapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class AvailabilitySlotItem(
    val timeLabel: String,
    val statusLabel: String,
    val isBooked: Boolean
)

class AvailabilitySlotAdapter(
    private var items: List<AvailabilitySlotItem>
) : RecyclerView.Adapter<AvailabilitySlotAdapter.SlotViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlotViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_availability_slot, parent, false)
        return SlotViewHolder(view)
    }

    override fun onBindViewHolder(holder: SlotViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(updatedItems: List<AvailabilitySlotItem>) {
        items = updatedItems
        notifyDataSetChanged()
    }

    class SlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val timeText: TextView = itemView.findViewById(R.id.slotTimeText)
        private val statusText: TextView = itemView.findViewById(R.id.slotStatusText)

        fun bind(item: AvailabilitySlotItem) {
            timeText.text = item.timeLabel
            statusText.text = item.statusLabel
            itemView.setBackgroundResource(
                if (item.isBooked) R.drawable.bg_slot_booked else R.drawable.bg_slot_available
            )
            statusText.setBackgroundResource(
                if (item.isBooked) R.drawable.bg_slot_status_booked else R.drawable.bg_slot_status_available
            )
        }
    }
}
