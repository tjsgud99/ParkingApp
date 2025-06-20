package com.example.ch_19_map

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.ImageButton
import android.widget.TextView

class NotificationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        val toolbarTitle = findViewById<TextView>(R.id.toolbarTitle)
        toolbarTitle.text = "공지사항 및 알림"
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        val recyclerView = findViewById<RecyclerView>(R.id.notificationRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val alertList = listOf(
            "앱이 최신 버전으로 업데이트 되었습니다.",
            "6월 10일(월) 서버 점검 예정입니다.",
            "새로운 기능이 추가되었습니다!"
        )
        recyclerView.adapter = NotificationAdapter(alertList)
    }
} 