package com.example.chatappserver.data.ipaddress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * IPアドレス監視処理 ViewModel
 * @param monitor   : IPアドレス監視処理インスタンス
 */
class IpAddressMonitorManager(monitor: IpAddressMonitor) : ViewModel() {

    // IPアドレス監視処理インスタンス
    private val ipAddressMonitor = monitor

    // IPアドレス状態管理
    private val _ipAddress = MutableStateFlow("init")
    val ipAddress = _ipAddress.asStateFlow()

    /**
     * ViewModel初期化処理
     */
    init {
        // IPアドレスの監視を開始
        ipAddressMonitor.startMonitoring()

        // IPアドレス状態を購読
        viewModelScope.launch {
            ipAddressMonitor.ipAddressState.collect { newIp ->
                println("The IP address value has changed")
                _ipAddress.value = newIp
            }
        }
    }

    /**
     * ViewModel破棄時処理
     */
    override fun onCleared() {
        super.onCleared()

        // IPアドレスの監視を停止
        ipAddressMonitor.stopMonitoring()
    }
}