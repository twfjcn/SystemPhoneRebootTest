package com.example.proxyrouteguard

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button

    private val statusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == RouteGuardService.ACTION_SERVICE_STATUS) {
                val running = intent.getBooleanExtra(RouteGuardService.EXTRA_RUNNING, false)
                updateUi(running)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tv_status)
        btnStart = findViewById(R.id.btn_start)
        btnStop = findViewById(R.id.btn_stop)

        registerReceiver(statusReceiver, IntentFilter(RouteGuardService.ACTION_SERVICE_STATUS),
            RECEIVER_NOT_EXPORTED)

        btnStart.setOnClickListener {
            val serviceIntent = Intent(this, RouteGuardService::class.java)
            startForegroundService(serviceIntent)
        }

        btnStop.setOnClickListener {
            val serviceIntent = Intent(this, RouteGuardService::class.java)
            stopService(serviceIntent)
        }

        updateUi(RouteGuardService.isRunning)
    }

    private fun updateUi(isRunning: Boolean) {
        if (isRunning) {
            tvStatus.text = "状态：守护正在运行"
            btnStart.isEnabled = false
            btnStop.isEnabled = true
        } else {
            tvStatus.text = "状态：未运行"
            btnStart.isEnabled = true
            btnStop.isEnabled = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(statusReceiver)
    }
}
