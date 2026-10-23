package com.dev.secretmemo.data.datasource

import android.content.SharedPreferences
import androidx.core.content.edit
import com.dev.secretmemo.data.model.Memo
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

interface MemoLocalDataSource {
    fun getMemos(): List<Memo>
    fun saveMemo(memo: Memo)
    fun deleteMemo(id: String)
    fun getMemoById(id: String): Memo?
}

class MemoLocalDataSourceImpl(
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson
) : MemoLocalDataSource {

    companion object {
        private const val KEY_MEMO_LIST = "KEY_MEMO_LIST"
    }

    override fun getMemos(): List<Memo> {
        val json = sharedPreferences.getString(KEY_MEMO_LIST, null) ?: return emptyList()
        val type = object : TypeToken<List<Memo>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    override fun saveMemo(memo: Memo) {
        val memos = getMemos().toMutableList()
        val index = memos.indexOfFirst { it.id == memo.id }
        if (index >= 0) {
            memos[index] = memo
        } else {
            memos.add(0, memo)
        }
        saveMemosToPrefs(memos)
    }

    override fun deleteMemo(id: String) {
        val memos = getMemos().toMutableList()
        memos.removeAll { it.id == id }
        saveMemosToPrefs(memos)
    }

    override fun getMemoById(id: String): Memo? {
        return getMemos().firstOrNull { it.id == id }
    }

    private fun saveMemosToPrefs(memos: List<Memo>) {
        val json = gson.toJson(memos)
        sharedPreferences.edit {
            putString(KEY_MEMO_LIST, json)
        }
    }
}
