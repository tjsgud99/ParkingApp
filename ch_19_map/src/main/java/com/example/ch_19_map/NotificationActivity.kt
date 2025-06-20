package com.example.ch_19_map

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.app.AlertDialog

class NotificationActivity : AppCompatActivity() {
    private val PREFS_NAME = "notifications"
    private lateinit var notificationAdapter: NotificationAdapter
    private val alertList = mutableListOf<String>()
    private var isAdmin = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val email = prefs.getString("email", "")
        isAdmin = email == "admin@parq.com"

        val toolbarTitle = findViewById<TextView>(R.id.toolbarTitle)
        toolbarTitle.text = getString(R.string.notification_title)
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        val recyclerView = findViewById<RecyclerView>(R.id.notificationRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        loadNotifications()
        notificationAdapter = NotificationAdapter(alertList, isAdmin, ::deleteNotification)
        recyclerView.adapter = notificationAdapter

        val adminWriteLayout = findViewById<View>(R.id.adminWriteLayout)
        val etNewNotification = findViewById<EditText>(R.id.etNewNotification)
        val btnAddNotification = findViewById<ImageButton>(R.id.btnAddNotification)
        if (isAdmin) {
            adminWriteLayout.visibility = View.VISIBLE
            btnAddNotification.setOnClickListener {
                val text = etNewNotification.text.toString().trim()
                if (text.isNotEmpty()) {
                    alertList.add(0, text)
                    saveNotifications()
                    notificationAdapter.notifyDataSetChanged()
                    etNewNotification.text.clear()
                } else {
                    Toast.makeText(this, "내용을 입력하세요", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            adminWriteLayout.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val email = prefs.getString("email", "")
        isAdmin = email == "admin@parq.com"

        val adminWriteLayout = findViewById<View>(R.id.adminWriteLayout)
        if (isAdmin) {
            adminWriteLayout.visibility = View.VISIBLE
        } else {
            adminWriteLayout.visibility = View.GONE
        }
        notificationAdapter.notifyDataSetChanged()
    }

    private fun loadNotifications() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val set = prefs.getStringSet("list", null)
        alertList.clear()
        if (set != null) {
            alertList.addAll(set.sortedByDescending { it })
        } else {
            // 기본 공지
            alertList.addAll(listOf(
                getString(R.string.default_notification1),
                getString(R.string.default_notification2),
                getString(R.string.default_notification3)
            ))
        }
    }

    private fun saveNotifications() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
        prefs.putStringSet("list", alertList.toSet())
        prefs.apply()
    }

    private fun deleteNotification(position: Int) {
        alertList.removeAt(position)
        saveNotifications()
        notificationAdapter.notifyDataSetChanged()
    }
} 