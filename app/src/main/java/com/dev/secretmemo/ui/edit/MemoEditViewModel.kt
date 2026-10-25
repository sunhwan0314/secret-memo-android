package com.dev.secretmemo.ui.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.secretmemo.data.model.Memo
import com.dev.secretmemo.data.repository.MemoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class MemoEditViewModel(
    private val memoRepository: MemoRepository
) : ViewModel() {

    private val _memo = MutableStateFlow<Memo?>(null)
    val memo: StateFlow<Memo?> = _memo.asStateFlow()

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()

    private var hasSaved = false

    fun loadMemo(id: String?) {
        if (id.isNullOrBlank()) return
        viewModelScope.launch {
            val loaded = memoRepository.getMemoById(id)
            _memo.value = loaded
        }
    }

    fun autoSaveMemo(
        existingId: String?,
        title: String,
        content: String,
        isPinned: Boolean,
        colorHex: String = "#FFFFFF"
    ) {
        if (hasSaved) return
        val currentTitle = title.trim()
        val currentContent = content.trim()

        if (currentTitle.isBlank() && currentContent.isBlank()) return

        val initialMemo = _memo.value
        if (initialMemo != null &&
            initialMemo.title == currentTitle &&
            initialMemo.content == currentContent &&
            initialMemo.isPinned == isPinned &&
            initialMemo.colorHex == colorHex
        ) {
            // No changes made
            return
        }

        hasSaved = true
        viewModelScope.launch {
            val memoToSave = Memo(
                id = existingId?.ifBlank { null } ?: UUID.randomUUID().toString(),
                title = currentTitle.ifBlank { "제목 없음" },
                content = currentContent,
                updatedAt = System.currentTimeMillis(),
                isPinned = isPinned,
                colorHex = colorHex
            )
            memoRepository.saveMemo(memoToSave)
            _isSaved.value = true
        }
    }

    fun deleteMemo(id: String) {
        hasSaved = true
        viewModelScope.launch {
            memoRepository.deleteMemo(id)
            _isSaved.value = true
        }
    }
}
