package com.example.proxyrouteguard

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
            Toast.makeText(this,"已发送守护启动请求",Toast.LENGTH_SHORT).show()
        }

        btnStop.setOnClickListener {
            val serviceIntent = Intent(this, RouteGuardService::class.java)
            stopService(serviceIntent)
            Toast.makeText(this,"已发送守护停止请求",Toast.LENGTH_SHORT).show()
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
