package com.lelestacia.nutapostest2.domain.model

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Transaction(
    val id: Int = 0,
    val receiver: String,
    val sender: String,
    val amount: Double,
    val description: String,
    val type: String,
    val image: Bitmap?,
    val date: Long
): Parcelable
