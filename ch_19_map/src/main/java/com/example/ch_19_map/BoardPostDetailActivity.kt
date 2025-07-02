package com.example.ch_19_map

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.widget.EditText
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide

class BoardPostDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_board_post_detail)

        val post = intent.getSerializableExtra("post") as? BoardPost
        val tvTitle = findViewById<TextView>(R.id.tvDetailTitle)
        val tvContent = findViewById<TextView>(R.id.tvDetailContent)
        val tvAuthor = findViewById<TextView>(R.id.tvDetailAuthor)
        val tvCreatedAt = findViewById<TextView>(R.id.tvDetailCreatedAt)
        val btnDelete = findViewById<Button>(R.id.btnDeletePost)
        val btnEdit = findViewById<Button>(R.id.btnEditPost)
        val tvReplyLabel = findViewById<TextView>(R.id.tvReplyLabel)
        val tvReplyContent = findViewById<TextView>(R.id.tvReplyContent)
        val layoutReplyInput = findViewById<View>(R.id.layoutReplyInput)
        val etReplyContent = findViewById<EditText>(R.id.etReplyContent)
        val btnSubmitReply = findViewById<Button>(R.id.btnSubmitReply)
        val ivImage = findViewById<ImageView>(R.id.ivPostImage)

        post?.let {
            tvTitle.text = it.title

            tvContent.text = it.content
            tvAuthor.text = "작성자: ${it.author.username}"
            tvCreatedAt.text = "작성일: ${it.createdAt}"
            // 이미지 표시
            val baseUrl = "http://10.0.2.2:8090" // 에뮬레이터 기준, 실기기는 PC의 IP로 변경
            val imageUrl = it.fileUrls?.firstOrNull()
            if (!imageUrl.isNullOrBlank()) {
                ivImage.visibility = View.VISIBLE
                Glide.with(this)
                    .load(baseUrl + imageUrl)
                    .into(ivImage)
            } else {
                ivImage.visibility = View.GONE
            }
        }

        // 관리자 여부 확인
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userId = prefs.getLong("user_id", -1L)
        val userRole = prefs.getString("role", "USER")
        val email = prefs.getString("email", "")
        val isAdmin = userRole?.uppercase() == "ADMIN" || userRole == "관리자" || email == "admin@parq.com"

        // 삭제/수정 버튼 노출 조건: 관리자 또는 본인만
        val canEditOrDelete = isAdmin || (post?.author?.id == userId)
        if (canEditOrDelete) {
            btnDelete.visibility = View.VISIBLE
            btnEdit.visibility = View.VISIBLE
            btnDelete.setOnClickListener {
                post?.let { p ->
                    lifecycleScope.launch {
                        try {
                            val response = RetrofitClient.getApiService(this@BoardPostDetailActivity).deleteBoardPost(p.id)
                            if (response.isSuccessful) {
                                Toast.makeText(this@BoardPostDetailActivity, "삭제되었습니다.", Toast.LENGTH_SHORT).show()
                                finish()
                            } else {
                                Toast.makeText(this@BoardPostDetailActivity, "삭제에 실패했습니다.", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(this@BoardPostDetailActivity, "네트워크 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            btnEdit.setOnClickListener {
                post?.let { p ->
                    val intent = Intent(this, BoardPostEditActivity::class.java)
                    intent.putExtra("post", p)
                    startActivity(intent)
                }
            }
        } else {
            btnDelete.visibility = View.GONE
            btnEdit.visibility = View.GONE
        }

        // 답변 조회
        post?.let { p ->
            lifecycleScope.launch {
                try {
                    val response = RetrofitClient.getApiService(this@BoardPostDetailActivity).getBoardReply(p.id)
                    if (response.isSuccessful && response.body() != null) {
                        val reply = response.body()!!
                        tvReplyLabel.visibility = View.VISIBLE
                        tvReplyContent.visibility = View.VISIBLE
                        tvReplyContent.text = "${reply.content}\n- ${reply.author.username} (${reply.createdAt})"
                    } else {
                        tvReplyLabel.visibility = View.GONE
                        tvReplyContent.visibility = View.GONE
                    }
                } catch (_: Exception) {
                    tvReplyLabel.visibility = View.GONE
                    tvReplyContent.visibility = View.GONE
                }
            }
        }

        // 관리자만 답변 작성 UI 노출
        if (isAdmin) {
            layoutReplyInput.visibility = View.VISIBLE
            btnSubmitReply.setOnClickListener {
                val replyText = etReplyContent.text.toString().trim()
                if (replyText.isEmpty()) {
                    Toast.makeText(this, "답변을 입력하세요.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                post?.let { p ->
                    lifecycleScope.launch {
                        try {
                            val req = BoardReplyRequest(p.id, userId, replyText)
                            val response = RetrofitClient.getApiService(this@BoardPostDetailActivity).createBoardReply(req)
                            if (response.isSuccessful) {
                                Toast.makeText(this@BoardPostDetailActivity, "답변이 등록되었습니다.", Toast.LENGTH_SHORT).show()
                                finish()
                            } else {
                                Toast.makeText(this@BoardPostDetailActivity, "답변 등록에 실패했습니다.", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(this@BoardPostDetailActivity, "네트워크 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        } else {
            layoutReplyInput.visibility = View.GONE
        }
    }
} 