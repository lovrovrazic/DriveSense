package com.example.drivesense

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.drivesense.HistoryListAdapter.HistoryViewHolder
import androidx.recyclerview.widget.ListAdapter

class HistoryListAdapter : ListAdapter<Record, HistoryViewHolder>(RecordsComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        return HistoryViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current.timestamp.toString())
    }

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val historyItemView: TextView = itemView.findViewById(R.id.textView)

        fun bind(text: String?) {
            historyItemView.text = text
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