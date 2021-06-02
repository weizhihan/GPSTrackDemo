package com.originWzh.gpstrackdemo

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.originWzh.gpstrackdemo.GPSService.GPSService


class MainActivity : AppCompatActivity() {
    private var service: GPSService? = null
    private var isBind = false
    lateinit var myBinder: GPSService.DataBinder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startGPSTrackService()

        val startTrackBtn = findViewById<TextView>(R.id.start_track_btn)

        startTrackBtn.setOnClickListener {
            myBinder?.start()
        }
    }


    /**
     * activity bind to the GPSService
     */
    private fun startGPSTrackService() {
        var intent = Intent(this, GPSService::class.java)
        startService(intent)
        bindService(intent, object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                isBind = true
                myBinder = service as GPSService.DataBinder
                Log.i("GPSTrack", "Activity - onServiceConnected")
            }

            override fun onServiceDisconnected(name: ComponentName) {
                isBind = false
                Log.i("GPSTrack", "Activity - onServiceDisconnected")
            }

        }, Context.BIND_AUTO_CREATE)
    }


}