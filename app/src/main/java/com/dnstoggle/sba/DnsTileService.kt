package com.dnstoggle.sba

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.provider.Settings
import android.util.Log
import android.graphics.drawable.Icon

class DnsTileService : TileService() {

    override fun onStartListening() {
        updateTile()
    }

    override fun onClick() {
        toggleDns()
        updateTile()
    }

    private fun updateTile() {
        qsTile?.apply {
            state = if (isDnsEnabled()) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            label = if (isDnsEnabled()) "AdGuard On" else "AdGuard Off"  // Set the label to "AdGuard" when enabled
            icon = if (isDnsEnabled()) Icon.createWithResource(this@DnsTileService, R.drawable.ic_dns_on)
            else Icon.createWithResource(this@DnsTileService, R.drawable.ic_dns_off)
            updateTile()
        }
    }

    private fun toggleDns() {
        val newDns = if (isDnsEnabled()) "" else "dns.adguard-dns.com"
        try {
            Settings.Global.putString(contentResolver, "private_dns_mode", if (newDns.isEmpty()) "off" else "hostname")
            Settings.Global.putString(contentResolver, "private_dns_specifier", newDns)
        } catch (e: SecurityException) {
            Log.e("DnsTileService", "Permission denied: WRITE_SECURE_SETTINGS required")
        }
    }

    private fun isDnsEnabled(): Boolean {
        return Settings.Global.getString(contentResolver, "private_dns_mode") == "hostname"
    }
}
