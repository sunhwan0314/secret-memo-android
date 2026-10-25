package com.dev.secretmemo

import com.dev.secretmemo.data.datasource.CounterLocalDataSource
import com.dev.secretmemo.data.model.Counter
import com.dev.secretmemo.data.repository.CounterRepositoryImpl
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CounterRepositoryTest {

    private lateinit var fakeDataSource: FakeCounterLocalDataSource
    private lateinit var repository: CounterRepositoryImpl

    @Before
    fun setUp() {
        fakeDataSource = FakeCounterLocalDataSource()
        repository = CounterRepositoryImpl(fakeDataSource)
    }

    @Test
    fun testSaveAndGetCounter() {
        val counter = Counter(id = "1", title = "Pushups", count = 0)
        repository.saveCounter(counter)

        val counters = repository.getCounters()
        assertEquals(1, counters.size)
        assertEquals("Pushups", counters[0].title)
    }

    @Test
    fun testIncrementAndDecrementAndReset() {
        val counter = Counter(id = "1", title = "Water Intake", count = 0)
        repository.saveCounter(counter)

        repository.incrementCounter("1")
        assertEquals(1, repository.getCounters()[0].count)

        repository.incrementCounter("1")
        assertEquals(2, repository.getCounters()[0].count)

        repository.decrementCounter("1")
        assertEquals(1, repository.getCounters()[0].count)

        repository.resetCounter("1")
        assertEquals(0, repository.getCounters()[0].count)
    }

    @Test
    fun testDeleteCounter() {
        val counter = Counter(id = "1", title = "Pushups", count = 5)
        repository.saveCounter(counter)
        assertEquals(1, repository.getCounters().size)

        repository.deleteCounter("1")
        assertEquals(0, repository.getCounters().size)
    }

    private class FakeCounterLocalDataSource : CounterLocalDataSource {
        private val counterMap = mutableMapOf<String, Counter>()

        override fun getCounters(): List<Counter> = counterMap.values.toList()

        override fun saveCounter(counter: Counter) {
            counterMap[counter.id] = counter
        }

        override fun deleteCounter(id: String) {
            counterMap.remove(id)
        }

        override fun getCounterById(id: String): Counter? = counterMap[id]
    }
}
