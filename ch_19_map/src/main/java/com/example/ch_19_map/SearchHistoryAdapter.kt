package com.example.ch_19_map

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SearchHistoryAdapter(
    private var historyList: List<SearchHistoryItem>,
    private var favoriteKeywords: Set<String>,
    private var isLoggedIn: Boolean,
    private val onItemClick: (SearchHistoryItem) -> Unit,
    private val onDeleteClick: (SearchHistoryItem) -> Unit,
    private val onFavoriteClick: (SearchHistoryItem) -> Unit,
    private val onLoginRequired: () -> Unit
) : RecyclerView.Adapter<SearchHistoryAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val keywordTextView: TextView = view.findViewById(R.id.tvKeyword)
        private val dateTextView: TextView = view.findViewById(R.id.tvDate)
        private val deleteButton: ImageView = view.findViewById(R.id.ivDelete)
        private val favoriteButton: ImageView = view.findViewById(R.id.ivFavorite)

        fun bind(item: SearchHistoryItem) {
            keywordTextView.text = item.keyword
            dateTextView.text = item.date
            
            if (isLoggedIn && favoriteKeywords.contains(item.keyword)) {
                favoriteButton.setImageResource(R.drawable.ic_favorite)
            } else {
                favoriteButton.setImageResource(R.drawable.ic_favorite_border)
            }

            itemView.setOnClickListener { onItemClick(item) }
            deleteButton.setOnClickListener { onDeleteClick(item) }
            favoriteButton.setOnClickListener {
                if (isLoggedIn) {
                    onFavoriteClick(item)
                } else {
                    onLoginRequired()
                }
            }
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
    
    fun updateList(newItems: List<SearchHistoryItem>, newFavorites: Set<String>, newIsLoggedIn: Boolean) {
        historyList = newItems
        favoriteKeywords = newFavorites
        isLoggedIn = newIsLoggedIn
        notifyDataSetChanged()
    }
} 