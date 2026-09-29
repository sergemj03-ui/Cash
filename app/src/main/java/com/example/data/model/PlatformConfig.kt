package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "platform_config")
data class PlatformConfigEntity(
    @PrimaryKey val id: String = "default_config",
    val usdToCdfRate: Double = 2800.0, // 1 USD = 2800 CDF
    val minWithdrawalUsd: Double = 3.0,
    val referralBonusRefereeThresholdTasks: Int = 1,
    val referralBonusAmountUsd: Double = 0.50,
    val maxSubmissionsPerHour: Int = 10,
    val isAntiFraudActive: Boolean = true,
    val totalPlatformPayoutsUsd: Double = 14250.0,
    val totalRevenueGeneratedUsd: Double = 38920.0,
    val activeSponsorsCount: Int = 18,
    val isMpesaActive: Boolean = true,
    val isAirtelActive: Boolean = true,
    val isOrangeActive: Boolean = true,
    val isAfrimoneyActive: Boolean = true,
    val isCardsActive: Boolean = true,
    val isPaypalActive: Boolean = true
)
