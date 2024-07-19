package com.lelestacia.nutapostest2.ui.tambah_transaksi

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lelestacia.nutapostest2.domain.model.Transaction
import com.lelestacia.nutapostest2.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TambahTransaksiViewModel(
    private val repository: TransactionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ViewState())
    val uiState: StateFlow<ViewState> = _uiState

    fun updateUri(uri: Uri) = viewModelScope.launch {
        if (uri != Uri.EMPTY) {
            _uiState.update {
                it.copy(
                    imageUri = uri,
                    imageBitmap = null,
                    isImageMenuVisible = true,
                    isImagePlaceholderTextVisible = false
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    imageUri = uri,
                    imageBitmap = null,
                    isImageMenuVisible = false,
                    isImagePlaceholderTextVisible = true
                )
            }
        }
    }

    fun showOrDismissImageDialog(visibility: Boolean) = viewModelScope.launch {
        _uiState.update {
            it.copy(
                isImageDialogVisible = visibility
            )
        }
    }

    fun showOrDismissFinanceTypeDialog(visibility: Boolean) = viewModelScope.launch {
        _uiState.update {
            it.copy(
                isFinanceTypeDialogVisible = visibility
            )
        }
    }

    fun showOrDismissSheetInformation(visibility: Boolean) = viewModelScope.launch {
        _uiState.update {
            it.copy(
                isInformationSheetVisible = visibility
            )
        }
    }

    fun saveTransaction(transaction: Transaction) = viewModelScope.launch {
        repository.saveTransaction(transaction)
        _uiState.update {
            it.copy(
                imageUri = Uri.EMPTY,
                imageBitmap = null
            )
        }
    }

    fun insertBitmap(bmp: Bitmap?) = viewModelScope.launch {
        _uiState.update {
            it.copy(
                imageBitmap = bmp,
                isImageMenuVisible = true,
                isImagePlaceholderTextVisible = false
            )
        }
    }

    fun resetState() = viewModelScope.launch {
        _uiState.update { ViewState() }
    }

    data class ViewState(
        val imageUri: Uri = Uri.EMPTY,
        val imageBitmap: Bitmap? = null,
        val isImageMenuVisible: Boolean = false,
        val isImagePlaceholderTextVisible: Boolean = true,
        val isImageDialogVisible: Boolean = false,
        val isFinanceTypeDialogVisible: Boolean = false,
        val isInformationSheetVisible: Boolean = false,
    )
}