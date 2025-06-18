package com.example.ch_19_map

data class SearchHistoryItem(
    val keyword: String,
    val date: String = "",
    val isFavorite: Boolean = false
) 