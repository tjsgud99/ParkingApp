package com.example.ch_19_map

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        findViewById<Button>(R.id.btnLogin).setOnClickListener {
            // 로그인 처리 (임시: 메인으로 이동)
            finish()
        }
        findViewById<LinearLayout>(R.id.btnKakao).setOnClickListener {
            // 카카오 로그인 연동 예정
        }
        findViewById<LinearLayout>(R.id.btnNaver).setOnClickListener {
            // 네이버 로그인 연동 예정
        }
        findViewById<TextView>(R.id.btnSignUp).setOnClickListener {
            // 회원가입 화면으로 이동
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }
} 