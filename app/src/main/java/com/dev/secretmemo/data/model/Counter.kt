package com.dev.secretmemo.data.model

import java.util.UUID

data class Counter(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val count: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)
