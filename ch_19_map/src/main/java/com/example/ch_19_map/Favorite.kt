package com.example.ch_19_map

import com.google.gson.annotations.SerializedName

data class Favorite(
    val id: Long,
    @SerializedName("user") val user: User,
    @SerializedName("parkingLot") val parkingLot: ParkingLotResponse
)

// User와 ParkingLotResponse는 이미 정의되어 있다고 가정합니다.
// 만약 없다면 아래와 같이 간단하게 정의할 수 있습니다.
/*
data class User(
    val id: Long,
    val username: String,
    val email: String
)

data class ParkingLotResponse(
    val id: Long,
    val name: String,
    val address: String,
    // ... other fields
    var latitude: Double = 0.0,
    var longitude: Double = 0.0
)
*/ 