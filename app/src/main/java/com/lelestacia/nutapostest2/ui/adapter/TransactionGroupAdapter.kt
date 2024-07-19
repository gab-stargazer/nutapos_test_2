package com.lelestacia.nutapostest2.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lelestacia.nutapostest2.R
import com.lelestacia.nutapostest2.databinding.ItemGroupTransactionBinding
import com.lelestacia.nutapostest2.domain.model.Transaction
import com.parassidhu.simpledate.toDateStandard
import java.text.NumberFormat
import java.util.Date
import java.util.Locale

class TransactionGroupAdapter(
    private val onEditTransaction: (Transaction) -> Unit,
    private val onDeleteTransaction: (Transaction) -> Unit,
) :
    ListAdapter<List<Transaction>, TransactionGroupAdapter.ViewHolder>(Companion) {

    inner class ViewHolder(private val binding: ItemGroupTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(transactions: List<Transaction>) {
            val context = binding.root.context
            binding.tvTransactionDate.text = Date(transactions.first().date).toDateStandard()
            var total = 0.toDouble()
            for (transaction in transactions) {
                if (transaction.type == context.getString(R.string.tv_another_income)) {
                    total += transaction.amount
                }
            }

            val localeID = Locale("in", "ID")
            val numberFormat = NumberFormat.getCurrencyInstance(localeID)
            binding.tvTransactionTotal.text = numberFormat.format(total)

            val adapter = TransactionAdapter(
                onEditTransaction = onEditTransaction,
                onDeleteTransaction = onDeleteTransaction
            )
            adapter.submitList(transactions)

            binding.rvTransaction.adapter = adapter
            binding.rvTransaction.layoutManager =
                LinearLayoutManager(binding.root.context, LinearLayoutManager.VERTICAL, false)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemGroupTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let {
            holder.bind(it)
        }
    }

    private companion object : DiffUtil.ItemCallback<List<Transaction>>() {
        override fun areItemsTheSame(
            oldItem: List<Transaction>,
            newItem: List<Transaction>,
        ): Boolean {
            return oldItem == newItem
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(
            oldItem: List<Transaction>,
            newItem: List<Transaction>,
        ): Boolean {
            return oldItem === newItem
        }
    }
}