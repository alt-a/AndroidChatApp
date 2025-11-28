package com.example.chatappserver

import android.app.Application
import com.example.chatappserver.data.ipaddress.IpAddressMonitor

/**
 * IPアドレス監視処理用 Application
 */
class MyApplication : Application() {

    // IPアドレス監視処理インスタンス取得
    val ipAddressMonitor by lazy {
        IpAddressMonitor(applicationContext)
    }

}