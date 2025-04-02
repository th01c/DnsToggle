package com.dnstoggle.sba

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.provider.Settings
import android.util.Log
import android.graphics.drawable.Icon
import android.widget.Toast

class DnsTileService : TileService() {
    private val adguardDns = "dns.adguard-dns.com"
    private val privateDnsMode = "private_dns_mode"
    private val privateDnsSpecifier = "private_dns_specifier"

    override fun onStartListening() {
        Log.d("DnsTileService", "onStartListening")
        updateTile()
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