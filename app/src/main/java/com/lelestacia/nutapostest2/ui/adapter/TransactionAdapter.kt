package com.lelestacia.nutapostest2.ui.adapter

import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lelestacia.nutapostest2.R
import com.lelestacia.nutapostest2.databinding.DialogViewImageBinding
import com.lelestacia.nutapostest2.databinding.ItemTransactionBinding
import com.lelestacia.nutapostest2.domain.model.Transaction
import com.lelestacia.nutapostest2.util.visible
import com.parassidhu.simpledate.toTimeStandard
import com.parassidhu.simpledate.toTimeStandardWithoutSeconds
import java.text.NumberFormat
import java.util.Date
import java.util.Locale

class TransactionAdapter(
    private val onEditTransaction: (Transaction) -> Unit,
    private val onDeleteTransaction: (Transaction) -> Unit,
) : ListAdapter<Transaction, TransactionAdapter.ViewHolder>(Companion) {

    inner class ViewHolder(private val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: Transaction) {

            val localeID = Locale("in", "ID")
            val numberFormat = NumberFormat.getCurrencyInstance(localeID)
            binding.tvTransactionValue.text = numberFormat.format(transaction.amount)
            binding.tvTransactionType.text = transaction.description

            if (binding.root.context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                binding.tvTransactionDate.text = Date(transaction.date).toTimeStandard()
                binding.tvRecipient?.text = transaction.receiver
                binding.tvSender?.text = transaction.sender
            } else {
                binding.tvTransactionTitle?.text = binding.root.context
                    .getString(
                        R.string.tv_sender_to_receiver,
                        transaction.sender,
                        transaction.receiver
                    )

                binding.tvTransactionDate.text =
                    Date(transaction.date).toTimeStandardWithoutSeconds()

                transaction.image?.let { bitmap ->
                    binding.btnViewPhoto?.visible()
                    binding.btnViewPhoto?.setOnClickListener {
                        val dialogBinding =
                            DialogViewImageBinding.inflate(LayoutInflater.from(binding.root.context))
                        dialogBinding.root.setImageBitmap(bitmap)

                        MaterialAlertDialogBuilder(binding.root.context, R.style.AlertDialogTheme)
                            .setView(dialogBinding.root)
                            .show()
                    }
                }
            }

            binding.btnDelete?.setOnClickListener {
                onDeleteTransaction(transaction)
            }

            binding.btnEdit?.setOnClickListener {
                onEditTransaction(transaction)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let {
            holder.bind(it)
        }
    }

    private companion object : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem == newItem
        }
    }
}
