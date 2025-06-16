package com.example.ch_19_map

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ParkingDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_parking_detail)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detail_main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val parkingDetail = intent.getParcelableExtra<ParkingDetail>("parking_detail_key")

        parkingDetail?.let { detail ->
            findViewById<TextView>(R.id.detail_name).text = "이름: ${detail.name}"
            findViewById<TextView>(R.id.detail_address).text = "주소: ${detail.address}"
            findViewById<TextView>(R.id.detail_total_spaces).text = detail.totalSpaces
            findViewById<TextView>(R.id.detail_available_spaces).text = detail.availableSpaces
            findViewById<TextView>(R.id.detail_latitude).text = "위도: ${detail.latitude}"
            findViewById<TextView>(R.id.detail_longitude).text = "경도: ${detail.longitude}"
            findViewById<TextView>(R.id.detail_center_id).text = "센터 ID: ${detail.centerId}"

            // 요금 정보 바인딩
            findViewById<TextView>(R.id.detail_basic_free_time).text = "기본 무료 시간: ${detail.basicFreeTime}"
            findViewById<TextView>(R.id.detail_basic_charge_time).text = "기본 요금 시간: ${detail.basicChargeTime}"
            findViewById<TextView>(R.id.detail_basic_charge).text = "기본 요금: ${detail.basicCharge}"
            findViewById<TextView>(R.id.detail_additional_unit_time).text = "추가 단위 시간: ${detail.additionalUnitTime}"
            findViewById<TextView>(R.id.detail_additional_unit_charge).text = "추가 단위 요금: ${detail.additionalUnitCharge}"
            findViewById<TextView>(R.id.detail_one_day_charge).text = "1일 요금: ${detail.oneDayCharge}"
            findViewById<TextView>(R.id.detail_monthly_charge).text = "월 정액 요금: ${detail.monthlyCharge}"

            // 운영 시간 정보 바인딩
            findViewById<TextView>(R.id.detail_sunday_oper_time).text = "일요일: ${detail.sundayOperTime}"
            findViewById<TextView>(R.id.detail_monday_oper_time).text = "월요일: ${detail.mondayOperTime}"
            findViewById<TextView>(R.id.detail_tuesday_oper_time).text = "화요일: ${detail.tuesdayOperTime}"
            findViewById<TextView>(R.id.detail_wednesday_oper_time).text = "수요일: ${detail.wednesdayOperTime}"
            findViewById<TextView>(R.id.detail_thursday_oper_time).text = "목요일: ${detail.thursdayOperTime}"
            findViewById<TextView>(R.id.detail_friday_oper_time).text = "금요일: ${detail.fridayOperTime}"
            findViewById<TextView>(R.id.detail_saturday_oper_time).text = "토요일: ${detail.saturdayOperTime}"
            findViewById<TextView>(R.id.detail_holiday_oper_time).text = "공휴일: ${detail.holidayOperTime}"
        }
    }
}