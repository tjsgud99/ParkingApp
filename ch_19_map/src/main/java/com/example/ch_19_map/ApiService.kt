package com.example.ch_19_map

import retrofit2.Response
import retrofit2.http.*

// 백엔드 API 인터페이스
interface ApiService {
    
    // 주차장 관련 API
    @GET("api/parkinglots")
    suspend fun getAllParkingLots(): Response<List<ParkingLotResponse>>
    
    @GET("api/parkinglots/{name}")
    suspend fun getParkingLotByName(@Path("name") name: String): Response<ParkingLotResponse>
    
    @POST("api/parkinglots")
    suspend fun registerParkingLot(@Body request: ParkingLotRequest): Response<String>
    
    // 사용자 관련 API
    @POST("api/user/register")
    suspend fun registerUser(@Body request: UserRegisterRequest): Response<UserResponse>
    
    @POST("api/user/login")
    suspend fun loginUser(@Body request: UserLoginRequest): Response<LoginResponse>
    
    // 즐겨찾기 관련 API
    @POST("api/favorites")
    suspend fun addFavorite(@Body request: FavoriteRequest): Response<String>
    
    @DELETE("api/favorites/{id}")
    suspend fun removeFavorite(@Path("id") id: Long): Response<String>
}

// 데이터 클래스들
data class ParkingLotResponse(
    val id: Long,
    val name: String,
    val address: String,
    val runtime: String,
    val total_space: Int,
    val fee: Int
)

data class ParkingLotRequest(
    val name: String,
    val address: String,
    val runtime: String,
    val total_space: Int,
    val fee: Int
)

data class UserRegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

data class UserLoginRequest(
    val email: String,
    val password: String
)

data class UserResponse(
    val success: Boolean,
    val message: String
)

data class FavoriteRequest(
    val userId: Long,
    val parkingLotId: Long
)

data class LoginResponse(
    val username: String,
    val email: String
) 