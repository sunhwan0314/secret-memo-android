package com.dev.secretmemo.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.secretmemo.data.model.Memo
import com.dev.secretmemo.data.repository.MemoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel(
    private val memoRepository: MemoRepository
) : ViewModel() {

    private val _memos = MutableStateFlow<List<Memo>>(emptyList())
    val memos: StateFlow<List<Memo>> = _memos.asStateFlow()

    private val _selectedDateMillis = MutableStateFlow<Long?>(null)
    val selectedDateMillis: StateFlow<Long?> = _selectedDateMillis.asStateFlow()

    private var currentQuery: String = ""

    fun loadMemos() {
        viewModelScope.launch {
            val filterDate = _selectedDateMillis.value
            val baseMemos = if (filterDate != null) {
                memoRepository.getMemosByDate(filterDate)
            } else {
                memoRepository.getMemos()
            }

            if (currentQuery.isBlank()) {
                _memos.value = baseMemos
            } else {
                _memos.value = baseMemos.filter {
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

    fun filterByDate(dateMillis: Long?) {
        _selectedDateMillis.value = dateMillis
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

    fun formatDate(millis: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date(millis))
    }
}
