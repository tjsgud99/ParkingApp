package com.example.ch_19_map

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ParkingLotResultAdapter(
    private var favoriteParkingLotIds: Set<Long>,
    private val onItemClick: (ParkingLotResponse) -> Unit,
    private val onFavoriteClick: (parkingLot: ParkingLotResponse, isCurrentlyFavorite: Boolean) -> Unit
) : RecyclerView.Adapter<ParkingLotResultAdapter.ViewHolder>() {

    private var results = listOf<ParkingLotResponse>()

    fun updateResults(newResults: List<ParkingLotResponse>) {
        results = newResults
        notifyDataSetChanged()
    }

    fun updateFavorites(newFavoriteIds: Set<Long>) {
        this.favoriteParkingLotIds = newFavoriteIds
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_parking_lot_result, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = results[position]
        holder.bind(item, favoriteParkingLotIds, onItemClick, onFavoriteClick)
    }

    override fun getItemCount(): Int = results.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val nameTextView: TextView = view.findViewById(R.id.tvParkingLotName)
        private val addressTextView: TextView = view.findViewById(R.id.tvParkingLotAddress)
        private val favoriteButton: ImageButton = view.findViewById(R.id.btnFavorite)

        fun bind(
            parkingLot: ParkingLotResponse,
            favoriteParkingLotIds: Set<Long>,
            onItemClick: (ParkingLotResponse) -> Unit,
            onFavoriteClick: (ParkingLotResponse, Boolean) -> Unit
        ) {
            nameTextView.text = parkingLot.name
            addressTextView.text = parkingLot.address
            itemView.setOnClickListener { onItemClick(parkingLot) }

            val isFavorite = favoriteParkingLotIds.contains(parkingLot.id)
            val favoriteIcon = if (isFavorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border
            favoriteButton.setImageResource(favoriteIcon)

            favoriteButton.setOnClickListener {
                android.util.Log.d("FavoriteTest", "Adapter: 하트 클릭됨 - parkingLotId=${parkingLot.id}, isFavorite=$isFavorite")
                onFavoriteClick(parkingLot, isFavorite)
            }
        }
    }
} 