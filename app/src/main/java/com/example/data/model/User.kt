package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String = "user_rdc_001",
    val name: String = "Serge Mukendi",
    val email: String = "serge.mukendi@example.cd",
    val phone: String = "+243 81 234 5678",
    val country: String = "RDC",
    val avatarUrl: String = "",
    val level: Int = 2,
    val xp: Int = 420,
    val nextLevelXp: Int = 800,
    val balanceUsd: Double = 8.50,
    val totalEarnedUsd: Double = 23.40,
    val earningsTodayUsd: Double = 1.80,
    val earningsThisWeekUsd: Double = 5.20,
    val earningsThisMonthUsd: Double = 18.50,
    val completedTasksCount: Int = 14,
    val referralCode: String = "CASHLINK-RD77",
    val referralCount: Int = 3,
    val referralBonusAvailableUsd: Double = 1.50,
    val currencyPreference: String = "USD", // "USD" or "CDF"
    val languagePreference: String = "fr",  // "fr" or "en"
    val isVerified: Boolean = true,
    val isFraudFlagged: Boolean = false,
    val deviceFingerprint: String = "DEVICE_SECURE_RDC_9021",
    val lastActiveTimestamp: Long = System.currentTimeMillis()
) {
    fun getLevelTitle(): String {
        return when (level) {
            1 -> "Débutant (Bronze)"
            2 -> "Actif (Argent)"
            3 -> "Expert (Or)"
            4 -> "Ambassadeur (Platine)"
            else -> "VIP (Diamant)"
        }
    }
}
