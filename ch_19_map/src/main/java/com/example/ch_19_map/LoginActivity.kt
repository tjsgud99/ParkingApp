package com.example.ch_19_map

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etEmail = findViewById<EditText>(R.id.etId)
        val etPassword = findViewById<EditText>(R.id.etPassword)

        findViewById<Button>(R.id.btnLogin).setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val request = UserLoginRequest(email, password)
                    val response = RetrofitClient.apiService.loginUser(request)
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            val loginResponse = response.body()
                            if (loginResponse != null) {
                                Log.d("LoginActivity", "Login Response: username=${loginResponse.username}, email=${loginResponse.email}")

                                val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
                                prefs.edit()
                                    .putString("username", loginResponse.username)
                                    .putString("email", loginResponse.email)
                                    .putString("password", password)
                                    .apply()
                                finish()
                            } else {
                                Toast.makeText(this@LoginActivity, "로그인 실패: 응답 없음", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this@LoginActivity, "로그인 실패: ${response.errorBody()?.string()}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Log.e("LoginActivity", "Network Error", e)
                        Toast.makeText(this@LoginActivity, "네트워크 오류: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
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