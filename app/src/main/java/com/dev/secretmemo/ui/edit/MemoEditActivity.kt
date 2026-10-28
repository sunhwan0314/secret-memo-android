package com.dev.secretmemo.ui.edit

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.dev.secretmemo.databinding.ActivityMemoEditBinding
import com.dev.secretmemo.util.ThemeManager
import com.dev.secretmemo.widget.MemoAppWidget
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class MemoEditActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_MEMO_ID = "EXTRA_MEMO_ID"
    }

    private lateinit var binding: ActivityMemoEditBinding
    private val viewModel: MemoEditViewModel by viewModel()
    private var memoId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMemoEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        memoId = intent.getStringExtra(EXTRA_MEMO_ID)

        setupListeners()
        setupBackNavigation()
        observeViewModel()

        if (memoId != null) {
            binding.btnDelete.visibility = View.VISIBLE
            viewModel.loadMemo(memoId)
        }
    }

    override fun onResume() {
        super.onResume()
        applyTheme()
    }

    private fun applyTheme() {
        val palette = ThemeManager.getThemePalette(this)
        binding.root.setBackgroundResource(palette.backgroundResId)
        binding.toolbarEdit.setBackgroundColor(palette.primaryColor)
        binding.toolbarEdit.title = "${palette.emoji} 메모 작성"
        binding.toolbarEdit.setTitleTextColor(android.graphics.Color.WHITE)
        window.statusBarColor = palette.primaryDarkColor
    }

    private fun setupListeners() {
        binding.toolbarEdit.setNavigationOnClickListener {
            performAutoSaveAndFinish()
        }

        binding.btnSave.setOnClickListener {
            performAutoSaveAndFinish()
        }

        binding.btnDelete.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("메모 삭제")
            .setMessage("이 메모를 정말 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                memoId?.let { id ->
                    viewModel.deleteMemo(id)
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                performAutoSaveAndFinish()
            }
        })
    }

    private fun performAutoSaveAndFinish() {
        val title = binding.etTitle.text.toString()
        val content = binding.etContent.text.toString()
        val isPinned = binding.switchPin.isChecked

        viewModel.autoSaveMemo(
            existingId = memoId,
            title = title,
            content = content,
            isPinned = isPinned
        )
        MemoAppWidget.updateAllWidgets(this)
        finish()
    }

    override fun onPause() {
        super.onPause()
        val title = binding.etTitle.text.toString()
        val content = binding.etContent.text.toString()
        val isPinned = binding.switchPin.isChecked

        viewModel.autoSaveMemo(
            existingId = memoId,
            title = title,
            content = content,
            isPinned = isPinned
        )
        MemoAppWidget.updateAllWidgets(this)
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.memo.collect { memo ->
                        memo?.let {
                            binding.etTitle.setText(it.title)
                            binding.etContent.setText(it.content)
                            binding.switchPin.isChecked = it.isPinned
                        }
                    }
                }
                launch {
                    viewModel.isSaved.collect { isSaved ->
                        if (isSaved) {
                            MemoAppWidget.updateAllWidgets(this@MemoEditActivity)
                            finish()
                        }
                    }
                }
            }
        }
    }
}
