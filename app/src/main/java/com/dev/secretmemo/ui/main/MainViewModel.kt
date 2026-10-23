package com.dev.secretmemo.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.secretmemo.data.model.Memo
import com.dev.secretmemo.data.repository.MemoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val memoRepository: MemoRepository
) : ViewModel() {

    private val _memos = MutableStateFlow<List<Memo>>(emptyList())
    val memos: StateFlow<List<Memo>> = _memos.asStateFlow()

    private var currentQuery: String = ""

    fun loadMemos() {
        viewModelScope.launch {
            val allMemos = memoRepository.getMemos()
            if (currentQuery.isBlank()) {
                _memos.value = allMemos
            } else {
                _memos.value = allMemos.filter {
                    it.title.contains(currentQuery, ignoreCase = true) ||
                            it.content.contains(currentQuery, ignoreCase = true)
                }
            }
        }
    }

    fun searchMemos(query: String) {
        currentQuery = query
        loadMemos()
    }

    fun deleteMemo(id: String) {
        viewModelScope.launch {
            memoRepository.deleteMemo(id)
            loadMemos()
        }
    }

    fun togglePin(id: String) {
        viewModelScope.launch {
            memoRepository.togglePin(id)
            loadMemos()
        }
    }
}
