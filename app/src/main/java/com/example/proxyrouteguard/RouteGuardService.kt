package com.example.proxyrouteguard

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RouteGuardService : Service() {
    private val TAG = "RouteGuardService"
    private var guardLoopJob: Job? = null

    private val tun = "tun0"
    private val dev = "wlan0"
    private val intervalMs = 3000L
    private val pref = 18000
    private val prefMinus1 = pref - 1
    private val containRule = "from all iif $dev lookup $tun"
    private val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    companion object {
        const val ACTION_SERVICE_STATUS = "com.example.proxyrouteguard.SERVICE_STATUS"
        const val EXTRA_RUNNING = "extra_running"
        var isRunning = false
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(1001, buildNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isRunning) {
            startGuardLoop()
        }
        return START_STICKY
    }

    private fun startGuardLoop() {
        isRunning = true
        sendStatusBroadcast(true)
        guardLoopJob?.cancel()
        guardLoopJob = CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            initNetworkRules()
            while (isActive) {
                val ipRuleResult = RootCmd.exec("ip rule")
                val ipRuleOutput = ipRuleResult.stdout

                if (!ipRuleOutput.contains(containRule)) {
                    val ipAddrResult = RootCmd.exec("ip ad")
                    val ipAddrOutput = ipAddrResult.stdout

                    if (!ipAddrOutput.contains(dev) || !ipAddrOutput.contains("state UP")) {
                        val timeStr = sdf.format(Date())
                        Log.i(TAG, "[$timeStr] dev has been lost.")
                    } else {
                        RootCmd.exec("ip rule add from all iif $dev table $tun pref $prefMinus1")
                        val timeStr = sdf.format(Date())
                        Log.i(TAG, "[$timeStr] network changed, reset the routing policy.")
                    }
                }
                delay(intervalMs)
            }
        }
    }

    private fun initNetworkRules() {
        Log.i(TAG, "initNetworkRules start")
        RootCmd.exec("sysctl -w net.ipv4.ip_forward=1")
        RootCmd.exec("iptables -F FORWARD")
        RootCmd.exec("iptables -t nat -A POSTROUTING -o $tun -j MASQUERADE")
        RootCmd.exec("ip rule add from all table main pref $pref")
        RootCmd.exec("ip rule add from all iif $dev table $tun pref $prefMinus1")
        Log.i(TAG, "initNetworkRules done")
    }

    private fun cleanNetworkRules() {
        Log.i(TAG, "cleanNetworkRules start")
        RootCmd.exec("ip rule del pref $prefMinus1")
        RootCmd.exec("ip rule del pref $pref")
        RootCmd.exec("iptables -t nat -D POSTROUTING -o $tun -j MASQUERADE")
        Log.i(TAG, "cleanNetworkRules done")
    }

    private fun sendStatusBroadcast(running: Boolean) {
        val intent = Intent(ACTION_SERVICE_STATUS)
        intent.putExtra(EXTRA_RUNNING, running)
        sendBroadcast(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        guardLoopJob?.cancel()
        cleanNetworkRules()
        isRunning = false
        sendStatusBroadcast(false)
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun buildNotification(): Notification {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, "ROUTE_GUARD_CHANNEL")
                .setContentTitle("路由守护运行中")
                .setContentText("Proxy路由策略守护")
                .setSmallIcon(R.mipmap.ic_launcher)
                .build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setContentTitle("路由守护运行中")
                .setContentText("Proxy路由策略守护")
                .setSmallIcon(R.mipmap.ic_launcher)
                .build()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "ROUTE_GUARD_CHANNEL",
                "路由守护服务",
                NotificationManager.IMPORTANCE_LOW
            )
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
