package com.dev.secretmemo.data.model

import java.util.UUID

data class Memo(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val updatedAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val colorHex: String = "#FFFFFF"
)
