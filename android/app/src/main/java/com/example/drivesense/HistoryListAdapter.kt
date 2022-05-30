package com.example.drivesense

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.drivesense.HistoryListAdapter.HistoryViewHolder
import androidx.recyclerview.widget.ListAdapter
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class HistoryListAdapter : ListAdapter<Record, HistoryViewHolder>(RecordsComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        return HistoryViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val historyItemView_time: TextView = itemView.findViewById(R.id.time_textView)
        private val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
        private val historyItemView_acc: TextView = itemView.findViewById(R.id.acc_sc_textView)
        private val historyItemView_break: TextView = itemView.findViewById(R.id.break_sc_textView)
        private val historyItemView_steer: TextView = itemView.findViewById(R.id.steer_sc_textView)
        private val historyItemView_speed: TextView = itemView.findViewById(R.id.speed_sc_textView)
        private val historyItemView_all: TextView = itemView.findViewById(R.id.all_sc_textView)
        private val historyItemView_e_time: TextView = itemView.findViewById(R.id.e_time_textView)

        fun bind(item: Record) {
            val time = LocalDateTime.ofInstant(Instant.ofEpochMilli(item.timestamp), ZoneId.systemDefault())
            val formatted = time.format(formatter)
            historyItemView_time.text = formatted
            historyItemView_acc.text = item.acceleration_score.toString()
            historyItemView_break.text = item.breaking_score.toString()
            historyItemView_steer.text = item.steering_score.toString()
            historyItemView_speed.text = item.speeding_score.toString()
            historyItemView_all.text = item.overall_score.toString()

            val minutes = item.elapsed_time / 1000 / 60
            val seconds = item.elapsed_time / 1000 % 60
            historyItemView_e_time.text = "$minutes min $seconds sec"
        }

        companion object {
            fun create(parent: ViewGroup): HistoryViewHolder {
                val view: View = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_item, parent, false)
                return HistoryViewHolder(view)
            }
        }
    }

    class RecordsComparator : DiffUtil.ItemCallback<Record>() {
        override fun areItemsTheSame(oldItem: Record, newItem: Record): Boolean {
            return oldItem === oldItem
        }

        override fun areContentsTheSame(oldItem: Record, newItem: Record): Boolean {
            return oldItem.timestamp == newItem.timestamp
        }
    }
}