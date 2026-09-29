package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType(val labelFr: String) {
    TASK_REWARD("Gain de tâche"),
    SURVEY_REWARD("Sondage rémunéré"),
    REFERRAL_BONUS("Bonus de parrainage"),
    LEVEL_UP_BONUS("Bonus palier niveau"),
    WITHDRAWAL("Demande de retrait"),
    REFUND("Remboursement retrait")
}

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val type: String, // TransactionType.name
    val amountUsd: Double,
    val isCredit: Boolean, // true for earnings, false for withdrawals
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "COMPLETE",
    val note: String = ""
)
