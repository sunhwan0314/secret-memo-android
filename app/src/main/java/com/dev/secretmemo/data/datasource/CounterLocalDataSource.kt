package com.dev.secretmemo.data.datasource

import android.content.SharedPreferences
import androidx.core.content.edit
import com.dev.secretmemo.data.model.Counter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

interface CounterLocalDataSource {
    fun getCounters(): List<Counter>
    fun saveCounter(counter: Counter)
    fun deleteCounter(id: String)
    fun getCounterById(id: String): Counter?
}

class CounterLocalDataSourceImpl(
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson
) : CounterLocalDataSource {

    companion object {
        private const val KEY_COUNTER_LIST = "KEY_COUNTER_LIST"
    }

    override fun getCounters(): List<Counter> {
        val json = sharedPreferences.getString(KEY_COUNTER_LIST, null) ?: return emptyList()
        val type = object : TypeToken<List<Counter>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    override fun saveCounter(counter: Counter) {
        val counters = getCounters().toMutableList()
        val index = counters.indexOfFirst { it.id == counter.id }
        if (index >= 0) {
            counters[index] = counter
        } else {
            counters.add(0, counter)
        }
        saveCountersToPrefs(counters)
    }

    override fun deleteCounter(id: String) {
        val counters = getCounters().toMutableList()
        counters.removeAll { it.id == id }
        saveCountersToPrefs(counters)
    }

    override fun getCounterById(id: String): Counter? {
        return getCounters().firstOrNull { it.id == id }
    }

    private fun saveCountersToPrefs(counters: List<Counter>) {
        val json = gson.toJson(counters)
        sharedPreferences.edit {
            putString(KEY_COUNTER_LIST, json)
        }
    }
}
