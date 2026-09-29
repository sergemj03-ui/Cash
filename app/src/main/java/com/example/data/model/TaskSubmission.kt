package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class SubmissionStatus(val labelFr: String, val labelEn: String) {
    PENDING("En attente", "Pending"),
    IN_REVIEW("Vérification", "Under Review"),
    APPROVED("Validée", "Approved"),
    CREDITED("Créditée", "Credited"),
    REJECTED("Refusée", "Rejected")
}

@Entity(tableName = "task_submissions")
data class TaskSubmissionEntity(
    @PrimaryKey val id: String,
    val taskId: String,
    val userId: String,
    val taskTitle: String,
    val category: String,
    val rewardUsd: Double,
    val status: String = SubmissionStatus.PENDING.name,
    val submittedAt: Long = System.currentTimeMillis(),
    val reviewNotes: String = "",
    val userProofInput: String = "",
    val antiFraudConfidenceScore: Int = 95 // 0 - 100
)
