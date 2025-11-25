package com.example.chatappclient.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * 入力文字数制限表示搭載 OutlinedTextField
 * @param value         : 入力フィールドに表示される値
 * @param onValueChange : 入力値変更時 コールバック
 * @param maxLength     : 最大文字数
 * @param modifier      : Modifier
 * @param enabled       : テキストフィールド有効/無効
 * @param singleLine    : 入力を単一行に制限するか否か
 * @param label         : 入力フィールド上部に表示されるラベル
 * @param placeholder   : 入力フィールドが空のときに表示されるヒントテキスト
 */
@Composable
fun MaxLengthErrorOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    maxLength: Int,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    singleLine: Boolean = false,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        visualTransformation = MaxLengthErrorTransformation(
            maxLength = maxLength,
            errorStyle = SpanStyle(color = MaterialTheme.colorScheme.error)
        ),
        modifier = modifier,
        enabled = enabled,
        singleLine = singleLine,
        label = label,
        placeholder = placeholder,
        isError = value.length > maxLength
    )
}

/**
 * 入力文字数上限超過検知 VisualTransformation
 * @param maxLength     : 最大文字数
 * @param errorStyle    : 文字数上限超過時 SpanStyle
 */
private class MaxLengthErrorTransformation(
    private val maxLength: Int,
    private val errorStyle: SpanStyle = SpanStyle(color = Color.Red)
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        return TransformedText(
            text = AnnotatedString(
                text = text.text,
                spanStyles =
                    if (text.length > maxLength) {
                    listOf(AnnotatedString.Range(errorStyle, maxLength, text.length))
                    }
                    else {
                        emptyList()
                    }
            ),
            offsetMapping =  OffsetMapping.Identity
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MaxLengthErrorTransformation) return false
        if (maxLength != other.maxLength || errorStyle != other.errorStyle) return false
        return true
    }

    override fun hashCode(): Int {
        return maxLength.hashCode()
    }
}