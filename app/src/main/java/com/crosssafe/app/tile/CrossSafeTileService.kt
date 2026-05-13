package com.crosssafe.app.tile

import android.content.Intent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.crosssafe.app.FlashActivity

class CrossSafeTileService : TileService() {

    override fun onClick() {
        val intent = Intent(this, FlashActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivityAndCollapse(intent)
    }

    override fun onStartListening() {
        qsTile?.state = Tile.STATE_INACTIVE
        qsTile?.label = "CrossSafe"
        qsTile?.updateTile()
    }
}
