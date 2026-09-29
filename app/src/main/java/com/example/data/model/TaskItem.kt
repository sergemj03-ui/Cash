package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TaskCategory(val labelFr: String, val labelEn: String) {
    ALL("Toutes", "All"),
    SURVEY("Sondages", "Surveys"),
    MICRO_TASK("Micro-tâches", "Micro-tasks"),
    APP_TEST("Tests d'apps", "App tests"),
    VIDEO_SPONSOR("Vidéos sponsorisées", "Sponsored videos"),
    PARTNER_OFFER("Offres partenaires", "Partner offers"),
    MISSION("Missions entreprises", "Corporate missions")
}

enum class TaskDifficulty {
    FACILE, MOYEN, AVANCE
}

enum class ProofType {
    SURVEY_FORM,        // Integrated questionnaire
    SCREENSHOT_UPLOAD,  // Proof description/upload
    APP_DOWNLOAD_TEST,  // Install, try & feedback
    VIDEO_WATCH,        // Watch 30s video with attention check
    LINK_VISIT          // Visit partner portal & validation code
}

@Entity(tableName = "tasks")
data class TaskItemEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val category: String, // String representation of TaskCategory
    val rewardUsd: Double,
    val estimatedMinutes: Int,
    val totalSlots: Int,
    val remainingSlots: Int,
    val conditions: String,
    val instructions: String,
    val proofType: String, // String representation of ProofType
    val sponsorName: String,
    val difficulty: String = TaskDifficulty.FACILE.name,
    val isActive: Boolean = true,
    val isFeatured: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
