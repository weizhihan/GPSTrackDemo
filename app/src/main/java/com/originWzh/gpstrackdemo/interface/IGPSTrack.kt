package com.originWzh.gpstrackdemo.`interface`

interface IGPSTrack {
    // 开始收集
    fun start()

    // 停止收集
    fun stop()

    // 暂停收集
    fun pause()

    // 保存当前的状态，和当前活动的Trip对象id至本地文件
    fun saveHoldStatus()

    //销毁
    fun destory()
}