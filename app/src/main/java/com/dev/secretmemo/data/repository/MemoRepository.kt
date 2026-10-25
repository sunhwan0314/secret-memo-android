package com.dev.secretmemo.data.repository

import com.dev.secretmemo.data.datasource.MemoLocalDataSource
import com.dev.secretmemo.data.model.Memo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

interface MemoRepository {
    fun getMemos(): List<Memo>
    fun getMemosByDate(dateMillis: Long): List<Memo>
    fun saveMemo(memo: Memo)
    fun deleteMemo(id: String)
    fun getMemoById(id: String): Memo?
    fun togglePin(id: String)
}

class MemoRepositoryImpl(
    private val localDataSource: MemoLocalDataSource
) : MemoRepository {

    override fun getMemos(): List<Memo> {
        return localDataSource.getMemos().sortedWith(
            compareByDescending<Memo> { it.isPinned }
                .thenByDescending { it.updatedAt }
        )
    }

    override fun getMemosByDate(dateMillis: Long): List<Memo> {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val targetDateStr = sdf.format(Date(dateMillis))
        return getMemos().filter { it.dateFormatted == targetDateStr }
    }

    override fun saveMemo(memo: Memo) {
        localDataSource.saveMemo(memo)
    }

    override fun deleteMemo(id: String) {
        localDataSource.deleteMemo(id)
    }

    override fun getMemoById(id: String): Memo? {
        return localDataSource.getMemoById(id)
    }

    override fun togglePin(id: String) {
        val memo = localDataSource.getMemoById(id) ?: return
        val updated = memo.copy(
            isPinned = !memo.isPinned,
            updatedAt = System.currentTimeMillis()
        )
        localDataSource.saveMemo(updated)
    }
}
