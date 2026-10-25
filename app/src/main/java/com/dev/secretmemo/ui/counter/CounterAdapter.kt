package com.dev.secretmemo.ui.counter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dev.secretmemo.data.model.Counter
import com.dev.secretmemo.databinding.ItemCounterBinding

class CounterAdapter(
    private val onIncrement: (Counter) -> Unit,
    private val onDecrement: (Counter) -> Unit,
    private val onReset: (Counter) -> Unit,
    private val onDelete: (Counter) -> Unit
) : ListAdapter<Counter, CounterAdapter.CounterViewHolder>(CounterDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CounterViewHolder {
        val binding = ItemCounterBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CounterViewHolder(binding, onIncrement, onDecrement, onReset, onDelete)
    }

    override fun onBindViewHolder(holder: CounterViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CounterViewHolder(
        private val binding: ItemCounterBinding,
        private val onIncrement: (Counter) -> Unit,
        private val onDecrement: (Counter) -> Unit,
        private val onReset: (Counter) -> Unit,
        private val onDelete: (Counter) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(counter: Counter) {
            binding.tvCounterTitle.text = counter.title
            binding.tvCount.text = counter.count.toString()

            binding.btnIncrement.setOnClickListener {
                onIncrement(counter)
            }

            binding.btnDecrement.setOnClickListener {
                onDecrement(counter)
            }

            binding.btnReset.setOnClickListener {
                onReset(counter)
            }

            binding.btnDeleteCounter.setOnClickListener {
                onDelete(counter)
            }
        }
    }

    object CounterDiffCallback : DiffUtil.ItemCallback<Counter>() {
        override fun areItemsTheSame(oldItem: Counter, newItem: Counter): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Counter, newItem: Counter): Boolean {
            return oldItem == newItem
        }
    }
}
