package com.example.ch_19_map

import retrofit2.http.GET
import retrofit2.http.Query
import com.example.ch_19_map.ParkingOperResponse

interface ParkingService {
    @GET("Parking/PrkSttusInfo")
    suspend fun getParkingList(
        @Query("serviceKey") serviceKey: String,
        @Query("pageNo") pageNo: Int = 1,
        @Query("numOfRows") numOfRows: Int = 10,
        @Query("format") format: Int = 2
    ): ParkingResponse

    @GET("Parking/PrkRealtimeInfo")
    suspend fun getParkingRealtimeList(
        @Query("serviceKey") serviceKey: String,
        @Query("pageNo") pageNo: Int = 1,
        @Query("numOfRows") numOfRows: Int = 10,
        @Query("format") format: Int = 2
    ): ParkingStatusResponse

    @GET("Parking/PrkOprInfo")
    suspend fun getParkingOperInfo(
        @Query("serviceKey") serviceKey: String,
        @Query("pageNo") pageNo: Int = 1,
        @Query("numOfRows") numOfRows: Int = 10,
        @Query("format") format: Int = 2
    ): ParkingOperResponse

}