package com.originWzh.gpstrackdemo.dataBase

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.text.TextUtils
import android.util.Log
import com.originWzh.gpstrackdemo.GPSService.GPSTrack
import com.originWzh.gpstrackdemo.bean.LocationInfo
import java.util.*

class GPSDBHelper(
    context: Context,
    name: String? = "GPS_track.db",
    version: Int = 1, factory: SQLiteDatabase.CursorFactory?
) : SQLiteOpenHelper(context, name, factory, version) {

    val TAG: String = GPSDBHelper::class.java.getName()
    private val TABLAE_NAME = "track"
    private var mStringBuffer: StringBuffer? = null
    private var mContentValues: ContentValues? = null // 要插入的数据包

    companion object {

        @Volatile
        private var instance: GPSDBHelper? = null

        fun getInstance(ctx: Context): GPSDBHelper {
            if (instance == null) {
                synchronized(GPSTrack::class) {
                    if (instance == null) {
                        instance = GPSDBHelper(ctx, version = 1, factory = null)
                    }
                }
            }
            return instance!!
        }
    }


    override fun onCreate(db: SQLiteDatabase?) {
        db!!.execSQL("create table $TABLAE_NAME(trackid varchar(64),tracktime varchar(20),latlngs text)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }

    /**
     * 添加GPS数据
     *
     * @param
     * @param trackid
     * @param tracktime
     * @param newLocSignData
     */
    fun addTrack(
        trackid: String?,
        tracktime: String?,
        newLocSignData: String?
    ) {
        Log.d(TAG, "addTrack start...")
        if (TextUtils.isEmpty(newLocSignData)) {
            Log.d(TAG, "Vector nodata")
            return
        }
        if (mStringBuffer == null) {
            mStringBuffer = StringBuffer()
        }
        var mDatabase: SQLiteDatabase? = null
        try {
            mDatabase = readableDatabase
            var cursor: Cursor? = null
            // 查找库里面有没有之前存储过当前trackid的数据
            if (!TextUtils.isEmpty(trackid)) {
                cursor = mDatabase.rawQuery(
                    "select * from " + TABLAE_NAME + " where trackid = ?",
                    arrayOf(trackid)
                )
            }
            // 如果之前存储过
            if (cursor != null && cursor.count > 0 && cursor.moveToFirst()) {
                val locSignData =
                    cursor.getString(cursor.getColumnIndex("locSignData"))
                if (!TextUtils.isEmpty(locSignData)) {
                    mStringBuffer?.append(locSignData)
                    Log.d(TAG, "old data:" + mStringBuffer.toString())
                }
                if (!TextUtils.isEmpty(newLocSignData)) {
                    mStringBuffer?.append(newLocSignData)
                    Log.d(TAG, "new data:" + mStringBuffer.toString())
                }
                if (mContentValues == null) {
                    mContentValues = ContentValues()
                }
                mContentValues?.clear()
                mContentValues?.put("trackid", trackid)
                mContentValues?.put("tracktime", tracktime)
                mContentValues?.put("locSignData", mStringBuffer.toString())
                mDatabase.update(
                    TABLAE_NAME,
                    mContentValues,
                    "trackid = ?",
                    arrayOf(trackid)
                )
                Log.d(TAG, "update data success")
            } else {
                if (mContentValues == null) {
                    mContentValues = ContentValues()
                }
                mContentValues?.clear()
                mContentValues?.put("trackid", trackid)
                mContentValues?.put("tracktime", tracktime)
                mContentValues?.put("locSignData", mStringBuffer?.append(newLocSignData).toString())
                Log.d(TAG, "init data:" + mStringBuffer.toString())
                mDatabase.insert(
                    TABLAE_NAME,
                    null,
                    mContentValues
                )
                Log.d(TAG, "init data success")
            }
        } catch (e: Exception) {
            Log.d(
                TAG, "addTrack error:$e"
            )
            e.printStackTrace()
        } finally {
            mDatabase?.close()
            if (mStringBuffer != null && !TextUtils.isEmpty(mStringBuffer.toString())) {
                mStringBuffer?.delete(0, mStringBuffer.toString().length)
            }
        }
        Log.d(TAG, "addTrack end...")
    }


    /**
     * 获取GPS数据
     *
     * @param
     * @param trackid
     * @param tracktime
     * @param newLocSignData
     */
    fun getGPSTrack(trackid: String?): List<LocationInfo?>? {
        Log.v("MYTAG", "getTrack start...")
        var mDatabase: SQLiteDatabase? = null
        var listTrack: MutableList<LocationInfo?>? = null
        try {
            mDatabase = readableDatabase
            var cursor: Cursor? = null
            // 查找库里面有没有之前存储过当前trackid的数据
            if (!TextUtils.isEmpty(trackid)) {
                cursor = mDatabase.rawQuery(
                    "select * from " + TABLAE_NAME + " where trackid = ?",
                    arrayOf(trackid)
                )
            }
            if (cursor != null && cursor.count > 0 && cursor.moveToFirst()) {
                Log.v("MYTAG", "hava data...")
                val latlngs = cursor.getString(cursor.getColumnIndex("latlngs"))
                if (!TextUtils.isEmpty(latlngs)) {
                    listTrack = ArrayList<LocationInfo?>()
                    val lonlats =
                        latlngs.split("\\|".toRegex()).toTypedArray()
                    if (lonlats != null && lonlats.size > 0) {
                        for (i in lonlats.indices) {
                            val lonlat = lonlats[i]
                            val split =
                                lonlat.split(",".toRegex()).toTypedArray()
                            if (split != null && split.size > 0) {
                                try {
                                    listTrack
                                        .add(
                                            LocationInfo(
                                                java.lang.Double.valueOf(split[0]),
                                                java.lang.Double.valueOf(split[1])
                                            )
                                        )
                                } catch (e: java.lang.Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            mDatabase?.close()
        }
        return listTrack
    }
}