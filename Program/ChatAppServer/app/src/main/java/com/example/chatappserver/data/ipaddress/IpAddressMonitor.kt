package com.example.chatappserver.data.ipaddress

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.Inet4Address

/**
 * IPアドレス監視処理クラス
 * @param appContext    : Androidフレームワークサービスアクセス用 Context
 */
class IpAddressMonitor(private val appContext: Context) {

    // IPアドレスを保持するための状態(State)変数
    private val _ipAddressState = MutableStateFlow("Detecting IP...")
    val ipAddressState = _ipAddressState.asStateFlow()

    // ネットワーク監視用のコールバック
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
            super.onLinkPropertiesChanged(network, linkProperties)
            Log.d("NetworkCallback", "LinkProperties changed: $linkProperties")
            updateIpAddress(linkProperties)
        }
        override fun onLost(network: Network) {
            super.onLost(network)
            // ネットワークが失われた場合、再度検出を試みる
            // (ただし、すぐ別のネットワークに切り替わるはずなので、ここではシンプルにIP未検出状態にする)
            _ipAddressState.value = "Network Lost. Re-detecting..."
            Log.d("NetworkCallback", "Network lost")
            // すぐにアクティブなネットワークを確認し直す
            try {
                val manager = appContext.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
                val activeNetwork = manager.activeNetwork
                if (activeNetwork != null) {
                    val props = manager.getLinkProperties(activeNetwork)
                    if (props != null) {
                        updateIpAddress(props)
                    }
                } else {
                    _ipAddressState.value = "No Active Network"
                }
            } catch (e: Exception) {
                Log.e("NetworkCallback", "Error re-checking IP on network lost", e)
            }
        }
    }

    /**
     * IPアドレス監視開始
     */
    fun startMonitoring() {
        Log.d("NetworkCallback", "Monitoring Start!")

        // IPアドレスの監視を開始
        val manager = appContext.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        try {
            // (a) 現在のIPアドレスを取得
            val currentNetwork = manager.activeNetwork
            val linkProperties = manager.getLinkProperties(currentNetwork)
            if (linkProperties != null) {
                updateIpAddress(linkProperties)
            } else {
                _ipAddressState.value = "No active network"
                Log.d("NetworkCallback", "No active network found initially")
            }
            // (b) ネットワーク状態変化の監視を開始
            manager.registerDefaultNetworkCallback(networkCallback)
        } catch (e: SecurityException) {
            Log.e("NetworkCallback", "Permission missing for initial check?", e)
            _ipAddressState.value = "Permission Error?"
        } catch (e: Exception) {
            Log.e("NetworkCallback", "Error during initial IP check", e)
            _ipAddressState.value = "Detection Error"
        }
    }

    /**
     * LinkPropertiesからIPアドレスを抽出し、Stateを更新する
     */
    private fun updateIpAddress(linkProperties: LinkProperties) {
        val newIp = linkProperties.linkAddresses
            .firstOrNull { it.address is Inet4Address && it.address.isSiteLocalAddress }
            ?.address?.hostAddress

        if (newIp != null) {
            _ipAddressState.value = newIp
            Log.d("NetworkCallback", "Found IP Address: $newIp")
        } else {
            _ipAddressState.value = "Not Found (Wi-Fi or Hotspot only)"
            Log.d("NetworkCallback", "IP not found in LinkProperties")
        }
    }

    /**
     * IPアドレス監視停止
     */
    fun stopMonitoring() {
        // ネットワーク監視を解除
        val manager = appContext.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        manager.unregisterNetworkCallback(networkCallback)
    }
}