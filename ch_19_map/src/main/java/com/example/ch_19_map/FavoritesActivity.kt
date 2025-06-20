package com.example.ch_19_map

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FavoritesActivity : AppCompatActivity() {

    private lateinit var favoritesAdapter: FavoritesAdapter
    private val favoritesList = mutableListOf<Favorite>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        setupRecyclerView()
        fetchFavorites()
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.favoritesRecyclerView)
        favoritesAdapter = FavoritesAdapter(favoritesList) { favorite ->
            removeFavorite(favorite)
        }
        recyclerView.adapter = favoritesAdapter
    }

    private fun getUserId(): Long {
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        return prefs.getLong("user_id", -1L)
    }

    private fun fetchFavorites() {
        val userId = getUserId()
        if (userId == -1L) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) { RetrofitClient.getApiService(this@FavoritesActivity).getFavorites(userId) }
                if (response.isSuccessful) {
                    response.body()?.let { list ->
                        withContext(Dispatchers.Main) {
                            favoritesList.clear()
                            favoritesList.addAll(list)
                            favoritesAdapter.notifyDataSetChanged()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@FavoritesActivity, "즐겨찾기 목록을 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("FavoritesActivity", "Error fetching favorites", e)
                    Toast.makeText(this@FavoritesActivity, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun removeFavorite(favorite: Favorite) {
        val userId = getUserId()
        val parkingLotId = favorite.parkingLot.id
        val request = FavoriteRequest(userId, parkingLotId)

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) { RetrofitClient.getApiService(this@FavoritesActivity).removeFavorite(request) }
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@FavoritesActivity, "즐겨찾기에서 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                        fetchFavorites() // 목록 새로고침
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@FavoritesActivity, "삭제에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("FavoritesActivity", "Error removing favorite", e)
                    Toast.makeText(this@FavoritesActivity, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
} 