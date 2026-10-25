package com.dev.secretmemo

import com.dev.secretmemo.data.datasource.MemoLocalDataSource
import com.dev.secretmemo.data.model.Memo
import com.dev.secretmemo.data.repository.MemoRepositoryImpl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar

class MemoRepositoryTest {

    private lateinit var fakeDataSource: FakeMemoLocalDataSource
    private lateinit var repository: MemoRepositoryImpl

    @Before
    fun setUp() {
        fakeDataSource = FakeMemoLocalDataSource()
        repository = MemoRepositoryImpl(fakeDataSource)
    }

    @Test
    fun testSaveAndGetMemos() {
        val memo = Memo(id = "1", title = "Test Memo", content = "Test Content")
        repository.saveMemo(memo)

        val memos = repository.getMemos()
        assertEquals(1, memos.size)
        assertEquals("Test Memo", memos[0].title)
    }

    @Test
    fun testGetMemosByDate() {
        val now = System.currentTimeMillis()
        val memoToday = Memo(id = "1", title = "Today Memo", content = "Content", updatedAt = now)

        val calPast = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -5)
        }
        val memoPast = Memo(id = "2", title = "Past Memo", content = "Content", updatedAt = calPast.timeInMillis)

        repository.saveMemo(memoToday)
        repository.saveMemo(memoPast)

        val filteredToday = repository.getMemosByDate(now)
        assertEquals(1, filteredToday.size)
        assertEquals("Today Memo", filteredToday[0].title)
    }

    @Test
    fun testTogglePinSortsPinnedFirst() {
        val memo1 = Memo(id = "1", title = "Memo 1", content = "C1", isPinned = false)
        val memo2 = Memo(id = "2", title = "Memo 2", content = "C2", isPinned = false)

        repository.saveMemo(memo1)
        repository.saveMemo(memo2)

        repository.togglePin("2")

        val memos = repository.getMemos()
        assertEquals("2", memos[0].id)
        assertTrue(memos[0].isPinned)
    }

    @Test
    fun testDeleteMemo() {
        val memo = Memo(id = "1", title = "Test Memo", content = "Test Content")
        repository.saveMemo(memo)
        assertEquals(1, repository.getMemos().size)

        repository.deleteMemo("1")
        assertEquals(0, repository.getMemos().size)
    }

    private class FakeMemoLocalDataSource : MemoLocalDataSource {
        private val memoMap = mutableMapOf<String, Memo>()

        override fun getMemos(): List<Memo> = memoMap.values.toList()

        override fun saveMemo(memo: Memo) {
            memoMap[memo.id] = memo
        }

        override fun deleteMemo(id: String) {
            memoMap.remove(id)
        }

        override fun getMemoById(id: String): Memo? = memoMap[id]
    }
}
