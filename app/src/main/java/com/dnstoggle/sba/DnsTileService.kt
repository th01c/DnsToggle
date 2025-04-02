package com.dnstoggle.sba

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.provider.Settings
import android.util.Log
import android.graphics.drawable.Icon
import android.widget.Toast
import android.app.Service
import android.content.Intent
import android.os.IBinder

class DnsTileService : TileService() {
    private val adguardDns = "dns.adguard-dns.com"
    private val privateDnsMode = "private_dns_mode"
    private val privateDnsSpecifier = "private_dns_specifier"

    override fun onStartListening() {
        Log.d("DnsTileService", "onStartListening")
        updateTile()
        //Start service to keep alive in background.
        startService(Intent(this, DnsKeepAliveService::class.java))
    }

    override fun onClick() {
        Log.d("DnsTileService", "onClick")
        toggleDns()
        updateTile()
    }

    private fun updateTile() {
        qsTile?.apply {
            val enabled = isDnsEnabled()
            state = if (enabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            label = if (enabled) "AdGuard On" else "AdGuard Off"

            if (enabled) {
                icon = Icon.createWithResource(this@DnsTileService, R.drawable.ic_dns_on)
            } else {
                icon = Icon.createWithResource(this@DnsTileService, R.drawable.ic_dns_off)
            }

            if (state == Tile.STATE_ACTIVE) {
                icon = Icon.createWithResource(this@DnsTileService, R.drawable.ic_dns_on)
            }

            updateTile()
        }
    }

    private fun toggleDns() {
        val newDns = if (isDnsEnabled()) "" else adguardDns
        try {
            Settings.Global.putString(contentResolver, privateDnsMode, if (newDns.isEmpty()) "off" else "hostname")
            Settings.Global.putString(contentResolver, privateDnsSpecifier, newDns)
        } catch (e: SecurityException) {
            Log.e("DnsTileService", "Permission denied: WRITE_SECURE_SETTINGS required", e)
            Toast.makeText(this, "Permission denied (ADB required)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isDnsEnabled(): Boolean {
        val mode = Settings.Global.getString(contentResolver, privateDnsMode)
        val specifier = Settings.Global.getString(contentResolver, privateDnsSpecifier)
        Log.d("DnsTileService", "isDnsEnabled mode: $mode specifier: $specifier")

        return mode == "hostname" && specifier == adguardDns
    }
}

class DnsKeepAliveService : Service() {

    override fun onCreate() {
        super.onCreate()
        Log.d("DnsKeepAliveService", "Service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("DnsKeepAliveService", "Service started")
        //Your service logic here.
        performKeepAliveTask()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("DnsKeepAliveService", "Service destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun performKeepAliveTask() {
        // Example: just log a message every 10 seconds.
        Thread {
            while(true) {
                try {
                    Log.d("DnsKeepAliveService", "Keep alive task running")
                    Thread.sleep(10000) // 10 seconds
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }.start()
    }
}