package com.originWzh.gpstrackdemo.GPSService

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import com.originWzh.gpstrackdemo.Wdtools
import com.originWzh.gpstrackdemo.`interface`.IGPSTrack
import com.originWzh.gpstrackdemo.bean.LocationInfo
import com.originWzh.gpstrackdemo.dataBase.GPSDBHelper
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class GPSTrack private constructor(ctx: Context, lm: LocationManager) : IGPSTrack {
    private val mLocations: Vector<LocationInfo>? = Vector()
    private var mDataBaseThread: ScheduledExecutorService? = null // 入库线程
    private var mVectorThread: ExecutorService? = null // 入缓存线程
    private var context: Context = ctx
    private var mLocationManager: LocationManager = lm


    companion object {

        @Volatile
        private var instance: GPSTrack? = null

        fun getInstance(ctx: Context, lm: LocationManager): GPSTrack {
            if (instance == null) {
                synchronized(GPSTrack::class) {
                    if (instance == null) {
                        instance = GPSTrack(ctx, lm)
                    }
                }
            }
            return instance!!
        }
    }

    override fun start() {
        checkPermissionsAndInitGps()
        startCollect()
    }

    override fun stop() {
        TODO("Not yet implemented")
    }

    override fun pause() {
        TODO("Not yet implemented")
    }

    override fun saveHoldStatus() {
        TODO("Not yet implemented")
    }

    override fun destory() {
        TODO("Not yet implemented")
    }

    private fun startCollect() {
        if (mDataBaseThread == null) {
            mDataBaseThread = Executors.newSingleThreadScheduledExecutor()
        }
        mDataBaseThread!!.scheduleWithFixedDelay({ // 取出缓存数据
            val stringBuffer = StringBuffer()
            for (i in mLocations!!.indices) {
                val locationInfo = mLocations[i]
                stringBuffer.append(locationInfo.getLat()).append(",")
                    .append(locationInfo.getLon())
                    .append("|")
            }
            // 取完之后清空数据
            mLocations.clear()
            val trackid =
                SimpleDateFormat("yyyy-MM-dd").format(Date())
            //每20S从缓存读一次数据并写入db
            GPSDBHelper.getInstance(context).addTrack(trackid, trackid, stringBuffer.toString())
        }, 1000 * 20.toLong(), 1000 * 20.toLong(), TimeUnit.MILLISECONDS)
    }

    /**
     * check permission and init the locationManager
     */
    private fun checkPermissionsAndInitGps() {
        if (ActivityCompat.checkSelfPermission(
                Wdtools.getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                Wdtools.getContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.e("GPSTrackDemo","please check the authority for location and read-write permission of this project")
            return
        }
        //位置提供器，也就是实际上来定位的对象，这里选择的是GPS定位,需要注意的是 这里必须在！！主线程中进行申请
        mLocationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            0,
            0f,
            locationListener
        )
    }

    /**
     * the listener of the GPS
     */
    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            Log.d("GPSTrack", "onLocationChanged")
            //TODO 记录GPS信息 存入缓存
            trackLocSignInfo(parseLocationInfo(location))
        }

        override fun onStatusChanged(
            provider: String,
            status: Int,
            extras: Bundle
        ) {
            Log.d("GPSTrack", "onStatusChanged")
        }

        override fun onProviderEnabled(provider: String) {
            Log.d("GPSTrack", "onProviderEnabled")
        }

        override fun onProviderDisabled(provider: String) {
            Log.d("GPSTrack", "onProviderDisabled")
        }
    }

    fun parseLocationInfo(location: Location): LocationInfo {
        var mLocationInfo: LocationInfo = LocationInfo(location.longitude, location.latitude)
        return mLocationInfo
    }


    fun trackLocSignInfo(data: LocationInfo?) {
        if (mVectorThread == null) {
            mVectorThread = Executors.newSingleThreadExecutor()
        }
        // 避免阻塞主线程，开一个单独线程来存入缓存
        mVectorThread?.execute { mLocations!!.add(data) }
    }
}