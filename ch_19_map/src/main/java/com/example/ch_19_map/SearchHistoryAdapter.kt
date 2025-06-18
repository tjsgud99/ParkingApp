package com.example.ch_19_map

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SearchHistoryAdapter(
    private var items: List<SearchHistoryItem>,
    private val onDeleteClick: (SearchHistoryItem) -> Unit = {},
    private val onItemClick: (SearchHistoryItem) -> Unit = {}
) : RecyclerView.Adapter<SearchHistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val keywordText: TextView = view.findViewById(R.id.tvKeyword)
        val dateText: TextView = view.findViewById(R.id.tvDate)
        val deleteBtn: ImageView = view.findViewById(R.id.btnDelete)
        val icon: ImageView = view.findViewById(R.id.ivIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.keywordText.text = item.keyword
        holder.dateText.text = item.date
        holder.icon.setImageResource(android.R.drawable.ic_menu_mylocation)
        holder.deleteBtn.setOnClickListener { onDeleteClick(item) }
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = items.size

    fun updateList(newItems: List<SearchHistoryItem>) {
        items = newItems
        notifyDataSetChanged()
    }
} 