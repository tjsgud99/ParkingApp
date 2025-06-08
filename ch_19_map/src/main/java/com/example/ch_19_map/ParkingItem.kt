package com.example.ch_19_map


data class ParkingStatusResponse(
    val PrkRealtimeInfo: List<ParkingStatusItem>
)

data class ParkingStatusItem(
    val pkfc_Available_ParkingLots_total: String?,
    val pkfc_ParkingLots_total: String?,
    val prk_center_id: String?
)

data class ParkingResponse(
    val PrkSttusInfo: List<ParkingItem>
)

data class ParkingItem(
    val prk_plce_nm: String,
    val prk_plce_adres: String,
    val prk_cmprt_co: String,
    val prk_plce_entrc_la: String,
    val prk_plce_entrc_lo: String,
    val prk_center_id: String?,
    val prk_cd: String?
)