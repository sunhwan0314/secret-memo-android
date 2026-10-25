package com.dev.secretmemo.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class Memo(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val updatedAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val colorHex: String = "#FFFFFF"
) {
    val dateFormatted: String
        get() {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return sdf.format(Date(updatedAt))
        }
}
