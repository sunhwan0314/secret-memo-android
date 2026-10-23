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

    fun loadMemo(id: String?) {
        if (id.isNullOrBlank()) return
        viewModelScope.launch {
            _memo.value = memoRepository.getMemoById(id)
        }
    }

    fun saveMemo(
        existingId: String?,
        title: String,
        content: String,
        isPinned: Boolean,
        colorHex: String
    ) {
        if (title.isBlank() && content.isBlank()) return

        viewModelScope.launch {
            val memoToSave = Memo(
                id = existingId?.ifBlank { null } ?: UUID.randomUUID().toString(),
                title = title.ifBlank { "제목 없음" },
                content = content,
                updatedAt = System.currentTimeMillis(),
                isPinned = isPinned,
                colorHex = colorHex
            )
            memoRepository.saveMemo(memoToSave)
            _isSaved.value = true
        }
    }

    fun deleteMemo(id: String) {
        viewModelScope.launch {
            memoRepository.deleteMemo(id)
            _isSaved.value = true
        }
    }
}
