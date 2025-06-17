package com.example.ch_19_map

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

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

@Parcelize
data class ParkingDetail(
    val name: String,
    val address: String,
    val totalSpaces: String,
    val availableSpaces: String,
    val latitude: Double,
    val longitude: Double,
    val centerId: String,
    val basicFreeTime: String? = "정보 없음",
    val basicChargeTime: String? = "정보 없음",
    val basicCharge: String? = "정보 없음",
    val additionalUnitTime: String? = "정보 없음",
    val additionalUnitCharge: String? = "정보 없음",
    val oneDayCharge: String? = "정보 없음",
    val monthlyCharge: String? = "정보 없음",
    val sundayOperTime: String? = "정보 없음",
    val mondayOperTime: String? = "정보 없음",
    val tuesdayOperTime: String? = "정보 없음",
    val wednesdayOperTime: String? = "정보 없음",
    val thursdayOperTime: String? = "정보 없음",
    val fridayOperTime: String? = "정보 없음",
    val saturdayOperTime: String? = "정보 없음",
    val holidayOperTime: String? = "정보 없음"
) : Parcelable

data class ParkingOperResponse(
    val PrkOprInfo: List<ParkingOperItem>
)

data class ParkingOperItem(
    val prk_center_id: String?,
    val basic_info: BasicInfo?,
    val fxamt_info: FxamtInfo?,
    val opertn_bs_free_time: String?,
    val Sunday: OpertnTime?,
    val Monday: OpertnTime?,
    val Tuesday: OpertnTime?,
    val Wednesday: OpertnTime?,
    val Thursday: OpertnTime?,
    val Friday: OpertnTime?,
    val Saturday: OpertnTime?,
    val Holiday: OpertnTime?
)

data class BasicInfo(
    val parking_chrge_bs_time: String?,
    val parking_chrge_bs_chrge: String?,
    val parking_chrge_adit_unit_chrge: String?,
    val parking_chrge_adit_unit_time: String?
)

data class FxamtInfo(
    val parking_chrge_mon_unit_chrge: String?,
    val parking_chrge_one_day_chrge: String?
)

data class OpertnTime(
    val opertn_start_time: String?,
    val opertn_end_time: String?
)