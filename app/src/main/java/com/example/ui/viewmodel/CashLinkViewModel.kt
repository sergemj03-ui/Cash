package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.PaymentMethod
import com.example.data.model.PlatformConfigEntity
import com.example.data.model.TaskCategory
import com.example.data.model.TaskItemEntity
import com.example.data.model.TaskSubmissionEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.UserEntity
import com.example.data.model.WithdrawalEntity
import com.example.data.repository.CashLinkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class UserActivityData(
    val submissions: List<TaskSubmissionEntity> = emptyList(),
    val withdrawals: List<WithdrawalEntity> = emptyList(),
    val transactions: List<TransactionEntity> = emptyList()
)

data class AdminOverviewData(
    val allWithdrawals: List<WithdrawalEntity> = emptyList(),
    val pendingSubmissions: List<TaskSubmissionEntity> = emptyList(),
    val allUsers: List<UserEntity> = emptyList()
)

class CashLinkViewModel(
    private val repository: CashLinkRepository
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow(TaskCategory.ALL)
    private val _searchQuery = MutableStateFlow("")
    private val _currentTab = MutableStateFlow(AppTab.DASHBOARD)
    private val _selectedTaskForDetail = MutableStateFlow<TaskItemEntity?>(null)
    private val _isExecutingTask = MutableStateFlow(false)
    private val _taskExecutionStep = MutableStateFlow(0)
    private val _activeInfoSheet = MutableStateFlow<InfoSheetType>(InfoSheetType.None)
    private val _isAdminMode = MutableStateFlow(false)
    private val _notificationMessage = MutableStateFlow<String?>(null)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _isSubmittingAction = MutableStateFlow(false)

    private val userActivityFlow: kotlinx.coroutines.flow.Flow<UserActivityData> = combine(
        repository.getUserSubmissionsFlow(),
        repository.getUserWithdrawalsFlow(),
        repository.getUserTransactionsFlow()
    ) { subs, withs, txs ->
        UserActivityData(subs, withs, txs)
    }

    private val adminDataFlow: kotlinx.coroutines.flow.Flow<AdminOverviewData> = combine(
        repository.getAllWithdrawalsFlow(),
        repository.getPendingSubmissionsFlow(),
        repository.getAllUsersFlow()
    ) { allWiths, pendSubs, users ->
        AdminOverviewData(allWiths, pendSubs, users)
    }

    private val userPrefsFlow = combine(
        _selectedCategory,
        _searchQuery,
        _currentTab,
        _isAdminMode
    ) { cat, query, tab, isAdmin ->
        Triple(cat, query, Pair(tab, isAdmin))
    }

    val uiState: StateFlow<CashLinkUiState> = combine(
        repository.getUserFlow(),
        repository.getTasksFlow(),
        userActivityFlow,
        repository.getPlatformConfigFlow(),
        adminDataFlow
    ) { user: UserEntity?, tasks: List<TaskItemEntity>, activity: UserActivityData, config: PlatformConfigEntity?, admin: AdminOverviewData ->
        val safeUser = user ?: UserEntity()
        val safeConfig = config ?: PlatformConfigEntity()

        val category = _selectedCategory.value
        val query = _searchQuery.value.trim().lowercase()

        val filtered = tasks.filter { task ->
            val matchesCategory = category == TaskCategory.ALL || task.category == category.name
            val matchesSearch = query.isEmpty() ||
                    task.title.lowercase().contains(query) ||
                    task.description.lowercase().contains(query) ||
                    task.sponsorName.lowercase().contains(query)
            matchesCategory && matchesSearch
        }

        val pendingWithdrawals = admin.allWithdrawals.filter { it.status == "PENDING" || it.status == "PROCESSING" }

        CashLinkUiState(
            currentUser = safeUser,
            tasks = tasks,
            filteredTasks = filtered,
            selectedCategory = category,
            searchQuery = _searchQuery.value,
            submissions = activity.submissions,
            withdrawals = activity.withdrawals,
            transactions = activity.transactions,
            platformConfig = safeConfig,
            currentTab = _currentTab.value,
            selectedTaskForDetail = _selectedTaskForDetail.value,
            isExecutingTask = _isExecutingTask.value,
            taskExecutionStep = _taskExecutionStep.value,
            activeInfoSheet = _activeInfoSheet.value,
            isAdminMode = _isAdminMode.value,
            adminPendingWithdrawals = pendingWithdrawals,
            adminPendingSubmissions = admin.pendingSubmissions,
            allUsers = admin.allUsers,
            userNotificationMessage = _notificationMessage.value,
            errorMessage = _errorMessage.value,
            isSubmittingAction = _isSubmittingAction.value
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CashLinkUiState()
    )

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun setCategory(category: TaskCategory) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun openTaskDetail(task: TaskItemEntity) {
        _selectedTaskForDetail.value = task
        _isExecutingTask.value = false
        _taskExecutionStep.value = 0
    }

    fun closeTaskDetail() {
        _selectedTaskForDetail.value = null
        _isExecutingTask.value = false
        _taskExecutionStep.value = 0
    }

    fun startTaskExecution() {
        _isExecutingTask.value = true
        _taskExecutionStep.value = 1
    }

    fun advanceTaskStep() {
        _taskExecutionStep.value += 1
    }

    fun submitTaskWithProof(proofText: String) {
        val task = _selectedTaskForDetail.value ?: return
        viewModelScope.launch {
            _isSubmittingAction.value = true
            val result = repository.submitTask(task.id, proofText, instantAutoVerify = true)
            _isSubmittingAction.value = false
            if (result.isSuccess) {
                _notificationMessage.value = "Tâche validée avec succès ! +${uiState.value.formatCurrency(task.rewardUsd)} crédités."
                closeTaskDetail()
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Erreur lors de la validation"
            }
        }
    }

    fun requestWithdrawal(
        amountUsd: Double,
        paymentMethod: PaymentMethod,
        recipientAccount: String,
        recipientName: String
    ) {
        viewModelScope.launch {
            _isSubmittingAction.value = true
            val config = repository.getPlatformConfig()
            val result = repository.requestWithdrawal(
                amountUsd = amountUsd,
                paymentMethodCode = paymentMethod.code,
                recipientAccount = recipientAccount,
                recipientName = recipientName,
                rateUsdToCdf = config.usdToCdfRate,
                feePercent = paymentMethod.feePercent,
                fixedFeeUsd = paymentMethod.fixedFeeUsd
            )
            _isSubmittingAction.value = false
            if (result.isSuccess) {
                _notificationMessage.value = "Demande de retrait transmise avec succès ! Traitement sous 24-48h."
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Erreur de retrait"
            }
        }
    }

    fun toggleCurrencyPreference() {
        val next = if (uiState.value.currentUser.currencyPreference == "USD") "CDF" else "USD"
        viewModelScope.launch {
            repository.setCurrencyPreference(next)
            _notificationMessage.value = "Devise changée en $next"
        }
    }

    fun toggleLanguagePreference() {
        val next = if (uiState.value.currentUser.languagePreference == "fr") "en" else "fr"
        viewModelScope.launch {
            repository.setLanguagePreference(next)
            _notificationMessage.value = if (next == "fr") "Langue changée en Français" else "Language changed to English"
        }
    }

    fun claimReferralBonus() {
        viewModelScope.launch {
            val result = repository.claimReferralBonus()
            if (result.isSuccess) {
                _notificationMessage.value = "Bonus de parrainage réclamé avec succès !"
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message
            }
        }
    }

    fun openInfoSheet(sheet: InfoSheetType) {
        _activeInfoSheet.value = sheet
    }

    fun closeInfoSheet() {
        _activeInfoSheet.value = InfoSheetType.None
    }

    fun toggleAdminMode() {
        _isAdminMode.value = !_isAdminMode.value
        _notificationMessage.value = if (_isAdminMode.value) "Mode Administrateur activé" else "Mode Utilisateur actif"
    }

    fun adminApproveWithdrawal(id: String) {
        viewModelScope.launch {
            val res = repository.adminApproveWithdrawal(id)
            if (res.isSuccess) {
                _notificationMessage.value = "Retrait #$id approuvé et marqué comme PAYÉ !"
            }
        }
    }

    fun adminRejectWithdrawal(id: String, reason: String) {
        viewModelScope.launch {
            val res = repository.adminRejectWithdrawal(id, reason)
            if (res.isSuccess) {
                _notificationMessage.value = "Retrait #$id rejeté et montant remboursé."
            }
        }
    }

    fun adminApproveSubmission(id: String) {
        viewModelScope.launch {
            val res = repository.adminApproveSubmission(id)
            if (res.isSuccess) {
                _notificationMessage.value = "Soumission approuvée et récompensée."
            }
        }
    }

    fun adminRejectSubmission(id: String, reason: String) {
        viewModelScope.launch {
            val res = repository.adminRejectSubmission(id, reason)
            if (res.isSuccess) {
                _notificationMessage.value = "Soumission rejetée."
            }
        }
    }

    fun adminAddNewTask(task: TaskItemEntity) {
        viewModelScope.launch {
            repository.adminAddTask(task)
            _notificationMessage.value = "Nouvelle tâche '${task.title}' publiée avec succès !"
        }
    }

    fun adminToggleFraudFlag(userId: String) {
        viewModelScope.launch {
            repository.adminToggleUserFraudFlag(userId)
            _notificationMessage.value = "Statut Anti-Fraude mis à jour pour $userId"
        }
    }

    fun clearNotification() {
        _notificationMessage.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }
}

class CashLinkViewModelFactory(private val repository: CashLinkRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CashLinkViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CashLinkViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
