package com.example.chatappclient.ui.component

/**
 * 送信先選択ダイアログ 個別送信チェック状態管理データクラス
 */
data class SelectRecipientCheckState(
    val id: Int,            // ユーザーID
    val isChecked: Boolean  // チェック状態
)