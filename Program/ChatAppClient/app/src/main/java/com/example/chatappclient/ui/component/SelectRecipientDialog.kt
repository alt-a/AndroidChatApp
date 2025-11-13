package com.example.chatappclient.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.chatappclient.ConnectionUser

// ----- 仮の送信先リスト -----
val tempList = mutableListOf(
    ConnectionUser(0, "あいうえおかきくけこさしすせそたちつてと"),
    ConnectionUser(1, "B"),
    ConnectionUser(2, "C"),
    ConnectionUser(3, "D"),
    ConnectionUser(4, "E"),
    ConnectionUser(5, "F"),
    ConnectionUser(6, "G"),
    ConnectionUser(7, "H"),
    ConnectionUser(8, "I"),
    ConnectionUser(9, "J"),
    ConnectionUser(10, "K"),
    ConnectionUser(11, "L")
)

/**
 * 送信先選択ダイアログ
 * @param recipientsList    : 送信先リスト（ユーザーID＆ユーザー名）
 * @param onDismissRequest  : 「戻る」ボタン押下時 画面遷移用コールバック
 * @param onSend            : 「送信」ボタン押下時 送信処理・画面遷移用コールバック
 */
@Composable
fun SelectRecipientDialog(
    recipientsList: List<ConnectionUser>,
    onDismissRequest: () -> Unit,
    onSend: () -> Unit
) {
    // ----- ラジオボタン用 -----
    // 要素
    val radioOptions = listOf("全員", "個別に送信")

    // 選択状態リスト
    // selectOption: 現在の選択状態（ゲッター）
    // onOptionSelected: 選択状態更新コールバック（セッター）
    val (selectOption, onOptionSelected) = remember { mutableStateOf(
        value = radioOptions[0]     // 初期値「全員」
    ) }

    // ----- チェックボックス用 -----
    // チェック状態リスト
    val checked = remember { mutableStateOf(
        recipientsList.map { item ->
            SelectRecipientCheckState(item.id, false)
        }
    ) }

    // 送信先リスト表示状態変数
    val showCheckboxState = remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                // ダイアログタイトル
                Text(
                    text = "送信先を選択",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                // 送信先種別選択ラジオボタン
                Column(
                    modifier = Modifier.selectableGroup()   // Columnをラジオボタンのグループに設定
                ) {
                    radioOptions.forEach { text ->
                        SelectRecipientTypeRadioButton(
                            label = text,
                            isSelected = (text == selectOption),    // 現在の選択状態を渡す
                            onSelect = {
                                onOptionSelected(text)  // 状態更新処理
                                if (text == "個別に送信") {
                                    showCheckboxState.value = true  // 送信先リスト表示
                                }
                                else {
                                    showCheckboxState.value = false // 送信先リスト非表示
                                }
                                println("RadioButton: ${text}") // TODO: デバッグ用 後で消す
                            }
                        )

                    }
                }
                // 「個別に送信」選択時 送信先選択チェックボックスリスト
                if (showCheckboxState.value) {
                    LazyColumn(     // スクロール可能なリスト
                        modifier = Modifier.heightIn(max = 400.dp)  // 高さ上限を設定
                    ) {
                        items(recipientsList) { item ->
                            // 現在のチェック状態を取得
                            val currentCheck = checked.value
                                .find { it.id == item.id }?.isChecked ?: false

                            SelectRecipientCheckbox(
                                label = item.name,
                                checked = currentCheck, // 現在のチェック状態を渡す
                                onCheckedChange = {
                                    // 状態更新処理
                                    checked.value = checked.value.map { check ->
                                        if (check.id == item.id) {
                                            // 該当IDの状態のみを更新した新しいリストを生成
                                            check.copy(isChecked = !check.isChecked)
                                        }
                                        else {
                                            check
                                        }
                                    }
                                    println("Checkbox${item.id}: ${checked.value}") // TODO: デバッグ用 後で消す
                                }
                            )
                        }
                    }
                }
                // ボタン
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 戻るボタン
                    TextButton(onClick = onDismissRequest) {
                        Text(
                            text = "戻る",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    // 送信ボタン
                    TextButton(onClick = onSend) {
                        Text(
                            text = "送信",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Preview(device = Devices.PIXEL_2)
@Composable
fun SelectRecipientDialogPreview() {
    SelectRecipientDialog(
        recipientsList = tempList,
        onSend = {},
        onDismissRequest = {}
    )
}

/**
 * 送信先種別選択ラジオボタン
 * @param label         : 選択肢ラベルテキスト
 * @param isSelected    : 現在の選択状態
 * @param onSelect      : ラジオボタンクリックイベントコールバック
 */
@Composable
private fun SelectRecipientTypeRadioButton(
    label: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .selectable(
                selected = isSelected,  // 現在の選択状態
                onClick = onSelect,     // クリックイベント
                role = Role.RadioButton // Row自体をラジオボタンとして操作できるように設定
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = null  // クリックイベントを直接処理することを防ぐ
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 送信先選択チェックボックス
 * @param label             : 選択肢ラベルテキスト
 * @param checked           : 現在のチェック状態
 * @param onCheckedChange   : チェックボックスクリックイベントコールバック
 */
@Composable
private fun SelectRecipientCheckbox(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp).padding(vertical = 6.dp)
            .clickable(
                onClick = { onCheckedChange(!checked) },    // クリックイベント
                role = Role.Checkbox    // Row自体をチェックボックスとして操作できるように設定
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,      // 現在のチェック状態
            onCheckedChange = null  // クリックイベントを直接処理することを防ぐ
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}