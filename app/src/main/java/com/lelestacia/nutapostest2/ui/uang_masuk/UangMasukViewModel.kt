package com.lelestacia.nutapostest2.ui.uang_masuk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lelestacia.nutapostest2.domain.model.Transaction
import com.lelestacia.nutapostest2.domain.repository.TransactionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UangMasukViewModel(
    private val repository: TransactionRepository,
) : ViewModel() {

    private val _dateRange: MutableStateFlow<Pair<Long, Long>> = MutableStateFlow(Pair(0L, 0L))

    @OptIn(ExperimentalCoroutinesApi::class)
    val transactions = _dateRange.flatMapLatest { dateRange_ ->
        repository.getTransactionBetweenRange(
            start = dateRange_.first,
            end = dateRange_.second
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        emptyList()
    )

    private val _viewState = MutableStateFlow(ViewState())
    val viewState = combine(
        transactions,
        _viewState
    ) { transactions: List<Transaction>, state: ViewState ->
        ViewState(
            dateRange = state.dateRange,
            transactions = transactions
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        ViewState()
    )

    fun setDateRange(dateRange: Pair<Long, Long>) = viewModelScope.launch {
        _dateRange.update { dateRange }
        _viewState.update {
            it.copy(
                dateRange = dateRange
            )
        }
    }

    fun deleteTransaction(transaction: Transaction) = viewModelScope.launch {
        repository.deleteTransaction(transaction)
    }

    data class ViewState(
        val dateRange: Pair<Long, Long> = Pair(0L, 0L),
        val transactions: List<Transaction> = emptyList(),
    )
}