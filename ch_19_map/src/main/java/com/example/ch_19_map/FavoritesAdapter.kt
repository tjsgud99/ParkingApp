package com.example.ch_19_map

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FavoritesAdapter(
    private var favorites: List<Favorite>,
    private val onRemoveClick: (Favorite) -> Unit
) : RecyclerView.Adapter<FavoritesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val favorite = favorites[position]
        holder.bind(favorite)
    }

    override fun getItemCount(): Int = favorites.size

    fun updateFavorites(newFavorites: List<Favorite>) {
        favorites = newFavorites
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val nameTextView: TextView = view.findViewById(R.id.tvFavoriteName)
        private val addressTextView: TextView = view.findViewById(R.id.tvFavoriteAddress)
        private val removeButton: ImageButton = view.findViewById(R.id.btnRemoveFavorite)

        fun bind(favorite: Favorite) {
            nameTextView.text = favorite.parkingLot.name
            addressTextView.text = favorite.parkingLot.address
            removeButton.setOnClickListener {
                onRemoveClick(favorite)
            }
        }
    }
} 