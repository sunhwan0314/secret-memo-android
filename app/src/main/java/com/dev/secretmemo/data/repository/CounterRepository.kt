package com.dev.secretmemo.data.repository

import com.dev.secretmemo.data.datasource.CounterLocalDataSource
import com.dev.secretmemo.data.model.Counter

interface CounterRepository {
    fun getCounters(): List<Counter>
    fun saveCounter(counter: Counter)
    fun deleteCounter(id: String)
    fun incrementCounter(id: String)
    fun decrementCounter(id: String)
    fun resetCounter(id: String)
}

class CounterRepositoryImpl(
    private val localDataSource: CounterLocalDataSource
) : CounterRepository {

    override fun getCounters(): List<Counter> {
        return localDataSource.getCounters().sortedByDescending { it.updatedAt }
    }

    override fun saveCounter(counter: Counter) {
        localDataSource.saveCounter(counter)
    }

    override fun deleteCounter(id: String) {
        localDataSource.deleteCounter(id)
    }

    override fun incrementCounter(id: String) {
        val counter = localDataSource.getCounterById(id) ?: return
        val updated = counter.copy(
            count = counter.count + 1,
            updatedAt = System.currentTimeMillis()
        )
        localDataSource.saveCounter(updated)
    }

    override fun decrementCounter(id: String) {
        val counter = localDataSource.getCounterById(id) ?: return
        val updated = counter.copy(
            count = counter.count - 1,
            updatedAt = System.currentTimeMillis()
        )
        localDataSource.saveCounter(updated)
    }

    override fun resetCounter(id: String) {
        val counter = localDataSource.getCounterById(id) ?: return
        val updated = counter.copy(
            count = 0,
            updatedAt = System.currentTimeMillis()
        )
        localDataSource.saveCounter(updated)
    }
}
