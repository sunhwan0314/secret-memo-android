package com.dev.secretmemo.ui.edit

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.dev.secretmemo.databinding.ActivityMemoEditBinding
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
        observeViewModel()

        if (memoId != null) {
            binding.btnDelete.visibility = View.VISIBLE
            viewModel.loadMemo(memoId)
        }
    }

    private fun setupListeners() {
        binding.toolbarEdit.setNavigationOnClickListener {
            finish()
        }

        binding.btnSave.setOnClickListener {
            saveAndExit()
        }

        binding.btnDelete.setOnClickListener {
            memoId?.let { id ->
                viewModel.deleteMemo(id)
            }
        }
    }

    private fun saveAndExit() {
        val title = binding.etTitle.text.toString().trim()
        val content = binding.etContent.text.toString().trim()
        val isPinned = binding.switchPin.isChecked

        viewModel.saveMemo(
            existingId = memoId,
            title = title,
            content = content,
            isPinned = isPinned,
            colorHex = "#FFFFFF"
        )
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
