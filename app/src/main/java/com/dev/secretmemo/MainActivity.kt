package com.dev.secretmemo

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
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
        applyTheme()
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
        mainViewModel.loadMemos()
        counterViewModel.loadCounters()
    }

    private fun applyTheme() {
        binding.toolbar.setBackgroundColor(Color.parseColor("#09090B"))
        binding.toolbar.title = if (isMemoTab) "담다" else "카운터 목록"
        window.statusBarColor = Color.parseColor("#000000")
        
        val navColorStateList = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf(-android.R.attr.state_checked)
            ),
            intArrayOf(
                Color.parseColor("#09090B"),
                Color.parseColor("#A1A1AA")
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
                    R.id.action_settings -> {
                        showSettingsMenuDialog()
                        true
                    }
                    else -> false
                }
            }
        }, this, Lifecycle.State.RESUMED)
    }

    private fun showSettingsMenuDialog() {
        val options = arrayOf(
            "개인정보 처리방침",
            "메모 백업하기",
            "앱 공유하기",
            "플레이스토어 평가하기",
            "앱 정보 및 라이선스"
        )

        AlertDialog.Builder(this)
            .setTitle("설정")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showPrivacyPolicyDialog()
                    1 -> backupMemosToShare()
                    2 -> shareApp()
                    3 -> rateApp()
                    4 -> showAboutAppDialog()
                }
            }
            .setNegativeButton("닫기", null)
            .show()
    }

    private fun showPrivacyPolicyDialog() {
        val policyText = """
            [담다 개인정보 처리방침]

            1. 개인정보 처리 목적
            담다 앱은 사용자의 개인 메모 및 카운터 데이터를 관리하기 위한 목적으로만 가동됩니다.

            2. 개인정보 저장 및 암호화
            작성된 모든 메모와 카운터 데이터는 기기 내부 안드로이드 보안 저장소(EncryptedSharedPreferences)에 AES-256 규격으로 안전하게 암호화되어 보존됩니다.

            3. 개인정보의 외부 전송 및 제3자 제공 금지
            본 앱은 어떠한 사용자 데이터나 메모 내용도 외부 서버로 전송, 수집, 공유하지 않습니다.

            4. 데이터 삭제
            사용자가 앱 내에서 메모 및 카운터를 삭제하면 기기 암호화 저장소에서 즉시 완전히 삭제됩니다.
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("개인정보 처리방침")
            .setMessage(policyText)
            .setPositiveButton("확인", null)
            .show()
    }

    private fun showAboutAppDialog() {
        val aboutText = """
            [담다 (Damda)]
            버전: 1.0.0 (v1)
            패키지: com.dev.secretmemo

            [사용된 오픈소스 라이선스]
            - AndroidX & Material3 Components (Apache 2.0)
            - Koin Framework (Apache 2.0)
            - Google Gson (Apache 2.0)
            - Jetpack Security Crypto (Apache 2.0)

            © 2025 Damda. All rights reserved.
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("앱 정보 및 라이선스")
            .setMessage(aboutText)
            .setPositiveButton("확인", null)
            .show()
    }

    private fun shareApp() {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "생각을 기록하고 간편하게 담아두는 앱 '담다'를 사용해 보세요!\nhttps://play.google.com/store/apps/details?id=$packageName")
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "담다 앱 공유하기")
        startActivity(shareIntent)
    }

    private fun rateApp() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
        } catch (_: Exception) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
        }
    }

    private fun backupMemosToShare() {
        val memos = mainViewModel.memos.value
        if (memos.isEmpty()) {
            Toast.makeText(this, "백업할 메모가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val sb = StringBuilder()
        sb.append("=== 담다 메모 백업 (${memos.size}개) ===\n\n")
        memos.forEachIndexed { index, memo ->
            sb.append("${index + 1}. [${memo.title}]\n")
            sb.append("${memo.content}\n")
            sb.append("작성일: ${memo.dateFormatted}\n\n")
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, sb.toString())
            type = "text/plain"
        }
        startActivity(Intent.createChooser(sendIntent, "메모 백업 공유"))
    }

    private fun setupBottomNav() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_memo -> {
                    isMemoTab = true
                    binding.layoutMemoContainer.visibility = View.VISIBLE
                    binding.layoutCounterContainer.visibility = View.GONE
                    binding.toolbar.title = "담다"
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
            .setMessage("'담다' 앱을 종료하시겠습니까?")
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
                            binding.tvFilterDate.text = "선택된 날짜: ${mainViewModel.formatDate(dateMillis)}"
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
