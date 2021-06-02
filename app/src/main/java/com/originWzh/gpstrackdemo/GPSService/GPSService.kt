package com.originWzh.gpstrackdemo.GPSService

import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Binder
import android.os.IBinder
import com.originWzh.gpstrackdemo.Wdtools
import com.originWzh.gpstrackdemo.`interface`.IGPSTrack

class GPSService : Service() {
    public var isTracked = false
    var lm = Wdtools.getContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager

    override fun onBind(intent: Intent?): IBinder? {
        return DataBinder()
    }

    override fun onCreate() {
        super.onCreate()

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()

    }

    inner class DataBinder : Binder(), IGPSTrack {
        override fun start() {
            if (isTracked) {
                return
            }
            GPSTrack.getInstance(Wdtools.getContext(), lm).start()
            isTracked = true
        }

        override fun stop() {
            isTracked = false
        }

        override fun pause() {}

        override fun saveHoldStatus() {}

        override fun destory() {}
    }
}