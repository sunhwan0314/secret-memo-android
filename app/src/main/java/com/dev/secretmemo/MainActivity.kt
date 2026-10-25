package com.dev.secretmemo

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.dev.secretmemo.databinding.ActivityMainBinding
import com.dev.secretmemo.ui.edit.MemoEditActivity
import com.dev.secretmemo.ui.main.MainViewModel
import com.dev.secretmemo.ui.main.MemoAdapter
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModel()
    private lateinit var adapter: MemoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupToolbar()
        setupRecyclerView()
        setupSearchView()
        setupCalendarView()
        setupFab()
        observeMemos()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadMemos()
    }

    private fun setupToolbar() {
        binding.toolbar.inflateMenu(R.menu.menu_main)
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            if (menuItem.itemId == R.id.action_calendar) {
                toggleCalendarView()
                true
            } else {
                false
            }
        }
    }

    private fun toggleCalendarView() {
        if (binding.calendarView.visibility == View.VISIBLE) {
            binding.calendarView.visibility = View.GONE
        } else {
            binding.calendarView.visibility = View.VISIBLE
        }
    }

    private fun setupRecyclerView() {
        adapter = MemoAdapter(
            onItemClick = { memo ->
                val intent = Intent(this, MemoEditActivity::class.java).apply {
                    putExtra(MemoEditActivity.EXTRA_MEMO_ID, memo.id)
                }
                startActivity(intent)
            },
            onPinClick = { memo ->
                viewModel.togglePin(memo.id)
            }
        )
        binding.rvMemos.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        binding.rvMemos.adapter = adapter
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.searchMemos(query.orEmpty())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.searchMemos(newText.orEmpty())
                return true
            }
        })
    }

    private fun setupCalendarView() {
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
            }
            viewModel.filterByDate(cal.timeInMillis)
        }

        binding.btnClearFilter.setOnClickListener {
            viewModel.filterByDate(null)
            binding.calendarView.visibility = View.GONE
        }
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            val intent = Intent(this, MemoEditActivity::class.java)
            startActivity(intent)
        }
    }

    private fun observeMemos() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.memos.collect { memos ->
                        adapter.submitList(memos) {
                            binding.rvMemos.scrollToPosition(0)
                        }
                        if (memos.isEmpty()) {
                            binding.tvEmpty.visibility = View.VISIBLE
                            binding.rvMemos.visibility = View.GONE
                        } else {
                            binding.tvEmpty.visibility = View.GONE
                            binding.rvMemos.visibility = View.VISIBLE
                        }
                    }
                }

                launch {
                    viewModel.selectedDateMillis.collect { dateMillis ->
                        if (dateMillis != null) {
                            binding.layoutFilterInfo.visibility = View.VISIBLE
                            binding.tvFilterDate.text = "📅 선택된 날짜: ${viewModel.formatDate(dateMillis)}"
                        } else {
                            binding.layoutFilterInfo.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }
}
