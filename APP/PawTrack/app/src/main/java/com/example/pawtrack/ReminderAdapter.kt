package com.example.pawtrack

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ReminderAdapter(
    private var alarms: List<AlarmItem>,
    private val onDeleteClickListener: (AlarmItem) -> Unit
) : RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reminder, parent, false)
        return ReminderViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        val currentItem = alarms[position]
        holder.bind(currentItem, onDeleteClickListener)
    }

    override fun getItemCount() = alarms.size

    class ReminderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewMessage: TextView = itemView.findViewById(R.id.textViewMessage)
        private val textViewTime: TextView = itemView.findViewById(R.id.textViewTime)
        private val checkBoxRepeat: CheckBox = itemView.findViewById(R.id.checkBoxRepeat)
        private val buttonDelete: Button = itemView.findViewById(R.id.buttonDelete)

        fun bind(item: AlarmItem, onDeleteClickListener: (AlarmItem) -> Unit) {
            textViewMessage.text = item.message
            textViewTime.text = item.time
            checkBoxRepeat.isChecked = item.repeat
            buttonDelete.setOnClickListener {
                onDeleteClickListener(item)
            }
        }
    }

    fun updateData(alarms: List<AlarmItem>) {
        this.alarms = alarms
        notifyDataSetChanged()
    }
}
