package com.example.ch_19_map

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MyInfoActivity : AppCompatActivity() {

    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvPassword: TextView
    private lateinit var btnBack: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_info)

        initializeViews()
        loadUserInfo()
        setupListeners()
    }

    private fun initializeViews() {
        tvName = findViewById(R.id.tvName)
        tvEmail = findViewById(R.id.tvEmail)
        tvPassword = findViewById(R.id.tvPassword)
        btnBack = findViewById(R.id.btnBack)
    }

    private fun loadUserInfo() {
        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "이름 없음")
        val email = sharedPreferences.getString("email", "이메일 없음")
        val password = sharedPreferences.getString("password", "비밀번호 없음")

        tvName.text = "이름: $username"
        tvEmail.text = "이메일: $email"
        tvPassword.text = "비밀번호: $password"
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }
    }
} 