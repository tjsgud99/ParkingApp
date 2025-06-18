package com.example.ch_19_map

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SearchResultAdapter(
    private var searchResults: List<SearchResult> = emptyList(),
    private val onItemClick: (SearchResult) -> Unit
) : RecyclerView.Adapter<SearchResultAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val placeNameTextView: TextView = view.findViewById(R.id.placeNameTextView)
        val addressTextView: TextView = view.findViewById(R.id.addressTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_result, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val searchResult = searchResults[position]
        holder.placeNameTextView.text = searchResult.placeName
        holder.addressTextView.text = searchResult.address
        
        holder.itemView.setOnClickListener {
            onItemClick(searchResult)
        }
    }

    override fun getItemCount() = searchResults.size

    fun updateResults(newResults: List<SearchResult>) {
        searchResults = newResults
        notifyDataSetChanged()
    }
} 