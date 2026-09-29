package com.example.ui.viewmodel

import com.example.data.model.PlatformConfigEntity
import com.example.data.model.TaskCategory
import com.example.data.model.TaskItemEntity
import com.example.data.model.TaskSubmissionEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.UserEntity
import com.example.data.model.WithdrawalEntity

enum class AppTab(val labelFr: String, val labelEn: String) {
    DASHBOARD("Accueil", "Home"),
    TASKS("Tâches", "Tasks"),
    WITHDRAW("Retrait", "Withdraw"),
    REFERRAL("Parrainage", "Referral"),
    PROFILE("Profil", "Profile")
}

sealed interface InfoSheetType {
    object None : InfoSheetType
    object HowItWorks : InfoSheetType
    object PaymentTerms : InfoSheetType
    object PrivacyPolicy : InfoSheetType
    object TermsOfService : InfoSheetType
    object SupportContact : InfoSheetType
}

data class CashLinkUiState(
    val currentUser: UserEntity = UserEntity(),
    val tasks: List<TaskItemEntity> = emptyList(),
    val filteredTasks: List<TaskItemEntity> = emptyList(),
    val selectedCategory: TaskCategory = TaskCategory.ALL,
    val searchQuery: String = "",
    val submissions: List<TaskSubmissionEntity> = emptyList(),
    val withdrawals: List<WithdrawalEntity> = emptyList(),
    val transactions: List<TransactionEntity> = emptyList(),
    val platformConfig: PlatformConfigEntity = PlatformConfigEntity(),
    val currentTab: AppTab = AppTab.DASHBOARD,
    val selectedTaskForDetail: TaskItemEntity? = null,
    val isExecutingTask: Boolean = false,
    val taskExecutionStep: Int = 0,
    val activeInfoSheet: InfoSheetType = InfoSheetType.None,
    val isAdminMode: Boolean = false,
    val adminPendingWithdrawals: List<WithdrawalEntity> = emptyList(),
    val adminPendingSubmissions: List<TaskSubmissionEntity> = emptyList(),
    val allUsers: List<UserEntity> = emptyList(),
    val userNotificationMessage: String? = null,
    val errorMessage: String? = null,
    val isSubmittingAction: Boolean = false
) {
    fun formatCurrency(amountUsd: Double): String {
        return if (currentUser.currencyPreference == "CDF") {
            val amountCdf = (amountUsd * platformConfig.usdToCdfRate).toInt()
            "%,d FC".format(amountCdf).replace(',', ' ')
        } else {
            "%.2f $".format(amountUsd)
        }
    }

    fun formatBothCurrencies(amountUsd: Double): String {
        val cdf = (amountUsd * platformConfig.usdToCdfRate).toInt()
        val formattedCdf = "%,d FC".format(cdf).replace(',', ' ')
        return "%.2f $ (%s)".format(amountUsd, formattedCdf)
    }
}
