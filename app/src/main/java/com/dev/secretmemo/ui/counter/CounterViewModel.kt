package com.dev.secretmemo.ui.counter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.secretmemo.data.model.Counter
import com.dev.secretmemo.data.repository.CounterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CounterViewModel(
    private val counterRepository: CounterRepository
) : ViewModel() {

    private val _counters = MutableStateFlow<List<Counter>>(emptyList())
    val counters: StateFlow<List<Counter>> = _counters.asStateFlow()

    fun loadCounters() {
        viewModelScope.launch {
            _counters.value = counterRepository.getCounters()
        }
    }

    fun createCounter(title: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val counter = Counter(title = title.trim())
            counterRepository.saveCounter(counter)
            loadCounters()
        }
    }

    fun increment(id: String) {
        viewModelScope.launch {
            counterRepository.incrementCounter(id)
            loadCounters()
        }
    }

    fun decrement(id: String) {
        viewModelScope.launch {
            counterRepository.decrementCounter(id)
            loadCounters()
        }
    }

    fun reset(id: String) {
        viewModelScope.launch {
            counterRepository.resetCounter(id)
            loadCounters()
        }
    }

    fun delete(id: String) {
        viewModelScope.launch {
            counterRepository.deleteCounter(id)
            loadCounters()
        }
    }
}
