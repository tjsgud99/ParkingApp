package com.example.ch_19_map

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SearchHistoryAdapter(
    private var historyList: List<SearchHistoryItem>,
    private val favoriteKeywords: Set<String>,
    private val onItemClick: (SearchHistoryItem) -> Unit,
    private val onDeleteClick: (SearchHistoryItem) -> Unit,
    private val onFavoriteClick: (SearchHistoryItem) -> Unit
) : RecyclerView.Adapter<SearchHistoryAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val keywordTextView: TextView = view.findViewById(R.id.tvKeyword)
        private val dateTextView: TextView = view.findViewById(R.id.tvDate)
        private val deleteButton: ImageView = view.findViewById(R.id.ivDelete)
        private val favoriteButton: ImageView = view.findViewById(R.id.ivFavorite)

        fun bind(item: SearchHistoryItem) {
            keywordTextView.text = item.keyword
            dateTextView.text = item.date
            
            if (favoriteKeywords.contains(item.keyword)) {
                favoriteButton.setImageResource(R.drawable.ic_favorite)
            } else {
                favoriteButton.setImageResource(R.drawable.ic_favorite_border)
            }

            itemView.setOnClickListener { onItemClick(item) }
            deleteButton.setOnClickListener { onDeleteClick(item) }
            favoriteButton.setOnClickListener { onFavoriteClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(historyList[position])
    }

    override fun getItemCount() = historyList.size

    fun updateList(newItems: List<SearchHistoryItem>) {
        historyList = newItems
        notifyDataSetChanged()
    }
    
    fun updateList(newItems: List<SearchHistoryItem>, newFavorites: Set<String>) {
        historyList = newItems
        (this.favoriteKeywords as MutableSet).clear()
        (this.favoriteKeywords as MutableSet).addAll(newFavorites)
        notifyDataSetChanged()
    }
} 