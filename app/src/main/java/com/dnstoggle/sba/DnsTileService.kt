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
        updateTile()
    }

    override fun onClick() {
        toggleDns()
        updateTile()
    }

    private fun updateTile() {
        qsTile?.apply {
            state = if (isDnsEnabled()) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            label = if (isDnsEnabled()) "AdGuard On" else "AdGuard Off"
            icon = if (isDnsEnabled()) Icon.createWithResource(this@DnsTileService, R.drawable.ic_dns_on)
            else Icon.createWithResource(this@DnsTileService, R.drawable.ic_dns_off)
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

        return mode == "hostname" && specifier == adguardDns
    }
}