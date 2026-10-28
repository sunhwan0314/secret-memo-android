package com.dev.secretmemo

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.dev.secretmemo.databinding.ActivityMainBinding
import com.dev.secretmemo.ui.counter.CounterAdapter
import com.dev.secretmemo.ui.counter.CounterViewModel
import com.dev.secretmemo.ui.edit.MemoEditActivity
import com.dev.secretmemo.ui.main.MainViewModel
import com.dev.secretmemo.ui.main.MemoAdapter
import com.dev.secretmemo.util.ThemeManager
import com.dev.secretmemo.util.ThemeMode
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val mainViewModel: MainViewModel by viewModel()
    private val counterViewModel: CounterViewModel by viewModel()

    private lateinit var memoAdapter: MemoAdapter
    private lateinit var counterAdapter: CounterAdapter

    private var isMemoTab = true

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
        setupBottomNav()
        setupMemoRecyclerView()
        setupCounterRecyclerView()
        setupSearchView()
        setupCalendarView()
        setupFab()
        setupBackNavigation()
        observeData()
    }

    override fun onResume() {
        super.onResume()
        applyTheme()
        mainViewModel.loadMemos()
        counterViewModel.loadCounters()
    }

    private fun applyTheme() {
        val palette = ThemeManager.getThemePalette(this)
        
        binding.toolbar.setBackgroundColor(palette.primaryColor)
        window.statusBarColor = palette.primaryDarkColor
        
        binding.fabAdd.backgroundTintList = ColorStateList.valueOf(palette.accentColor)
        binding.layoutFilterInfo.setBackgroundColor(palette.accentColor)
        
        val navColorStateList = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf(-android.R.attr.state_checked)
            ),
            intArrayOf(
                palette.primaryColor,
                0x8A000000.toInt()
            )
        )
        binding.bottomNav.itemIconTintList = navColorStateList
        binding.bottomNav.itemTextColor = navColorStateList
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_main, menu)
            }

            override fun onPrepareMenu(menu: Menu) {
                super.onPrepareMenu(menu)
                val calendarItem = menu.findItem(R.id.action_calendar)
                calendarItem?.isVisible = isMemoTab
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_calendar -> {
                        toggleCalendarView()
                        true
                    }
                    R.id.action_theme_settings -> {
                        showThemeSettingsDialog()
                        true
                    }
                    else -> false
                }
            }
        }, this, Lifecycle.State.RESUMED)
    }

    private fun showThemeSettingsDialog() {
        val modes = ThemeMode.entries.toTypedArray()
        val items = modes.map { it.displayName }.toTypedArray()
        val currentMode = ThemeManager.getSavedThemeMode(this)
        val selectedIndex = modes.indexOf(currentMode).coerceAtLeast(0)

        var tempSelectedIndex = selectedIndex

        AlertDialog.Builder(this)
            .setTitle("🎨 앱 테마 설정")
            .setSingleChoiceItems(items, selectedIndex) { _, which ->
                tempSelectedIndex = which
            }
            .setPositiveButton("적용") { _, _ ->
                val newMode = modes[tempSelectedIndex]
                ThemeManager.saveThemeMode(this, newMode)
                applyTheme()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun setupBottomNav() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_memo -> {
                    isMemoTab = true
                    binding.layoutMemoContainer.visibility = View.VISIBLE
                    binding.layoutCounterContainer.visibility = View.GONE
                    binding.toolbar.title = "시크릿 메모"
                    invalidateOptionsMenu()
                    true
                }
                R.id.nav_counter -> {
                    isMemoTab = false
                    binding.layoutMemoContainer.visibility = View.GONE
                    binding.layoutCounterContainer.visibility = View.VISIBLE
                    binding.layoutCalendarContainer.visibility = View.GONE
                    binding.toolbar.title = "카운터 목록"
                    invalidateOptionsMenu()
                    true
                }
                else -> false
            }
        }
    }

    private fun toggleCalendarView() {
        if (binding.layoutCalendarContainer.visibility == View.VISIBLE) {
            binding.layoutCalendarContainer.visibility = View.GONE
        } else {
            binding.layoutCalendarContainer.visibility = View.VISIBLE
        }
    }

    private fun setupMemoRecyclerView() {
        memoAdapter = MemoAdapter(
            onItemClick = { memo ->
                val intent = Intent(this, MemoEditActivity::class.java).apply {
                    putExtra(MemoEditActivity.EXTRA_MEMO_ID, memo.id)
                }
                startActivity(intent)
            },
            onPinClick = { memo ->
                mainViewModel.togglePin(memo.id)
            }
        )
        binding.rvMemos.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        binding.rvMemos.adapter = memoAdapter
    }

    private fun setupCounterRecyclerView() {
        counterAdapter = CounterAdapter(
            onIncrement = { counter ->
                counterViewModel.increment(counter.id)
            },
            onDecrement = { counter ->
                counterViewModel.decrement(counter.id)
            },
            onReset = { counter ->
                counterViewModel.reset(counter.id)
            },
            onDelete = { counter ->
                showDeleteCounterConfirmationDialog(counter.id, counter.title)
            }
        )
        binding.rvCounters.layoutManager = LinearLayoutManager(this)
        binding.rvCounters.adapter = counterAdapter
    }

    private fun showDeleteCounterConfirmationDialog(id: String, title: String) {
        AlertDialog.Builder(this)
            .setTitle("카운터 삭제")
            .setMessage("'$title' 카운터를 정말 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                counterViewModel.delete(id)
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.layoutCalendarContainer.visibility == View.VISIBLE) {
                    binding.layoutCalendarContainer.visibility = View.GONE
                } else {
                    showAppExitConfirmationDialog()
                }
            }
        })
    }

    private fun showAppExitConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("앱 종료")
            .setMessage("시크릿 메모 앱을 종료하시겠습니까?")
            .setPositiveButton("종료") { _, _ ->
                finish()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (isMemoTab) {
                    mainViewModel.searchMemos(query.orEmpty())
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (isMemoTab) {
                    mainViewModel.searchMemos(newText.orEmpty())
                }
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
            mainViewModel.filterByDate(cal.timeInMillis)
        }

        binding.btnClearFilter.setOnClickListener {
            mainViewModel.filterByDate(null)
            binding.layoutCalendarContainer.visibility = View.GONE
        }
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            if (isMemoTab) {
                val intent = Intent(this, MemoEditActivity::class.java)
                startActivity(intent)
            } else {
                showAddCounterDialog()
            }
        }
    }

    private fun showAddCounterDialog() {
        val editText = EditText(this).apply {
            hint = "카운터 이름 (예: 운동 횟수)"
            setPadding(48, 32, 48, 32)
        }

        AlertDialog.Builder(this)
            .setTitle("새 카운터 추가")
            .setView(editText)
            .setPositiveButton("생성") { _, _ ->
                val name = editText.text.toString().trim()
                if (name.isNotBlank()) {
                    counterViewModel.createCounter(name)
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun observeData() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    mainViewModel.memos.collect { memos ->
                        memoAdapter.submitList(memos) {
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
                    mainViewModel.selectedDateMillis.collect { dateMillis ->
                        if (dateMillis != null) {
                            binding.layoutFilterInfo.visibility = View.VISIBLE
                            binding.tvFilterDate.text = "📅 선택된 날짜: ${mainViewModel.formatDate(dateMillis)}"
                        } else {
                            binding.layoutFilterInfo.visibility = View.GONE
                        }
                    }
                }

                launch {
                    counterViewModel.counters.collect { counters ->
                        counterAdapter.submitList(counters)
                        if (counters.isEmpty()) {
                            binding.tvCounterEmpty.visibility = View.VISIBLE
                            binding.rvCounters.visibility = View.GONE
                        } else {
                            binding.tvCounterEmpty.visibility = View.GONE
                            binding.rvCounters.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }
}
