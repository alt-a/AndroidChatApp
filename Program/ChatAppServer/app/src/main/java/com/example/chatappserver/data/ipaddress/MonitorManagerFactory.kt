package com.example.chatappserver.data.ipaddress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * IPアドレス監視処理 ViewModelへ依存関係を提供するファクトリ
 * @param monitor   : IPアドレス監視処理インスタンス
 */
class MonitorManagerFactory(private val monitor: IpAddressMonitor) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(IpAddressMonitorManager::class.java)) {
            return IpAddressMonitorManager(monitor) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}