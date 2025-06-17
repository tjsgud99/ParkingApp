package com.example.ch_19_map

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class NavigationDialogActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation_dialog)

        val latitude = intent.getDoubleExtra("latitude", 0.0)
        val longitude = intent.getDoubleExtra("longitude", 0.0)

        // 네이버 지도 버튼
        findViewById<Button>(R.id.btnNaverMap).setOnClickListener {
            openNaverMap(latitude, longitude)
        }

        // 카카오내비 버튼
        findViewById<Button>(R.id.btnKakaoNavi).setOnClickListener {
            openKakaoNavi(latitude, longitude)
        }

        // 티맵 버튼
        findViewById<Button>(R.id.btnTmap).setOnClickListener {
            openTmap(latitude, longitude)
        }
    }

    private fun openNaverMap(latitude: Double, longitude: Double) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("nmap://route/public?dlat=$latitude&dlng=$longitude&dname=목적지"))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "네이버 지도가 설치되어 있지 않습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openKakaoNavi(latitude: Double, longitude: Double) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("kakaonavi://route?ep=$latitude,$longitude&ep_name=목적지"))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "카카오내비가 설치되어 있지 않습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openTmap(latitude: Double, longitude: Double) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("tmap://route?goalname=목적지&goalx=$longitude&goaly=$latitude"))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "티맵이 설치되어 있지 않습니다.", Toast.LENGTH_SHORT).show()
        }
    }
} 