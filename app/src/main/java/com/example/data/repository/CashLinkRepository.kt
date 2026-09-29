package com.example.data.repository

import com.example.data.local.CashLinkDao
import com.example.data.model.PlatformConfigEntity
import com.example.data.model.SubmissionStatus
import com.example.data.model.TaskItemEntity
import com.example.data.model.TaskSubmissionEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.model.UserEntity
import com.example.data.model.WithdrawalEntity
import com.example.data.model.WithdrawalStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID

class CashLinkRepository(private val dao: CashLinkDao) {

    val currentUserId = "user_rdc_001"

    fun getUserFlow(): Flow<UserEntity?> = dao.getUserFlow(currentUserId)
    fun getAllUsersFlow(): Flow<List<UserEntity>> = dao.getAllUsersFlow()
    fun getTasksFlow(): Flow<List<TaskItemEntity>> = dao.getAllActiveTasksFlow()
    fun getAllTasksForAdminFlow(): Flow<List<TaskItemEntity>> = dao.getAllTasksFlow()
    fun getUserSubmissionsFlow(): Flow<List<TaskSubmissionEntity>> = dao.getUserSubmissionsFlow(currentUserId)
    fun getAllSubmissionsFlow(): Flow<List<TaskSubmissionEntity>> = dao.getAllSubmissionsFlow()
    fun getPendingSubmissionsFlow(): Flow<List<TaskSubmissionEntity>> = dao.getPendingSubmissionsFlow()
    fun getUserWithdrawalsFlow(): Flow<List<WithdrawalEntity>> = dao.getUserWithdrawalsFlow(currentUserId)
    fun getAllWithdrawalsFlow(): Flow<List<WithdrawalEntity>> = dao.getAllWithdrawalsFlow()
    fun getUserTransactionsFlow(): Flow<List<TransactionEntity>> = dao.getUserTransactionsFlow(currentUserId)
    fun getPlatformConfigFlow(): Flow<PlatformConfigEntity?> = dao.getPlatformConfigFlow()

    suspend fun getPlatformConfig(): PlatformConfigEntity {
        return withContext(Dispatchers.IO) {
            dao.getPlatformConfig() ?: PlatformConfigEntity()
        }
    }

    suspend fun submitTask(
        taskId: String,
        proofText: String,
        instantAutoVerify: Boolean = true
    ): Result<TaskSubmissionEntity> = withContext(Dispatchers.IO) {
        val user = dao.getUser(currentUserId) ?: return@withContext Result.failure(Exception("Utilisateur introuvable"))
        if (user.isFraudFlagged) {
            return@withContext Result.failure(Exception("Compte suspendu pour activité suspecte (Anti-Fraude)."))
        }

        val task = dao.getTaskById(taskId) ?: return@withContext Result.failure(Exception("Tâche introuvable"))
        if (task.remainingSlots <= 0) {
            return@withContext Result.failure(Exception("Toutes les places pour cette tâche sont épuisées."))
        }

        val submissionId = "sub_" + UUID.randomUUID().toString().take(8)

        // Initial submission starts at PENDING -> IN_REVIEW
        val initialStatus = if (instantAutoVerify) SubmissionStatus.IN_REVIEW.name else SubmissionStatus.PENDING.name
        val submission = TaskSubmissionEntity(
            id = submissionId,
            taskId = task.id,
            userId = user.id,
            taskTitle = task.title,
            category = task.category,
            rewardUsd = task.rewardUsd,
            status = initialStatus,
            submittedAt = System.currentTimeMillis(),
            userProofInput = proofText,
            antiFraudConfidenceScore = 98
        )
        dao.insertSubmission(submission)

        // Decrement slot
        val updatedTask = task.copy(remainingSlots = (task.remainingSlots - 1).coerceAtLeast(0))
        dao.updateTask(updatedTask)

        // If auto-verify is enabled (for instant verified micro-tasks/surveys MVP experience)
        if (instantAutoVerify) {
            creditSubmissionReward(submissionId)
        }

        Result.success(submission)
    }

    suspend fun creditSubmissionReward(submissionId: String): Boolean = withContext(Dispatchers.IO) {
        val submission = dao.getSubmissionById(submissionId) ?: return@withContext false
        val user = dao.getUser(submission.userId) ?: return@withContext false

        // Update submission status to APPROVED then CREDITED
        val creditedSubmission = submission.copy(
            status = SubmissionStatus.CREDITED.name,
            reviewNotes = "Validé par le système anti-fraude CashLink RDC"
        )
        dao.updateSubmission(creditedSubmission)

        // Update user balance, XP, and completed tasks
        val newXp = user.xp + (submission.rewardUsd * 100).toInt() + 20
        var newLevel = user.level
        var nextLevelXp = user.nextLevelXp
        if (newXp >= nextLevelXp) {
            newLevel = (user.level + 1).coerceAtMost(5)
            nextLevelXp = newLevel * 600
        }

        val updatedUser = user.copy(
            balanceUsd = user.balanceUsd + submission.rewardUsd,
            totalEarnedUsd = user.totalEarnedUsd + submission.rewardUsd,
            earningsTodayUsd = user.earningsTodayUsd + submission.rewardUsd,
            earningsThisWeekUsd = user.earningsThisWeekUsd + submission.rewardUsd,
            earningsThisMonthUsd = user.earningsThisMonthUsd + submission.rewardUsd,
            completedTasksCount = user.completedTasksCount + 1,
            xp = newXp,
            level = newLevel,
            nextLevelXp = nextLevelXp
        )
        dao.updateUser(updatedUser)

        // Record transaction
        dao.insertTransaction(
            TransactionEntity(
                id = "tx_" + UUID.randomUUID().toString().take(8),
                userId = user.id,
                title = submission.taskTitle,
                type = TransactionType.TASK_REWARD.name,
                amountUsd = submission.rewardUsd,
                isCredit = true
            )
        )

        true
    }

    suspend fun requestWithdrawal(
        amountUsd: Double,
        paymentMethodCode: String,
        recipientAccount: String,
        recipientName: String,
        rateUsdToCdf: Double = 2800.0,
        feePercent: Double = 1.5,
        fixedFeeUsd: Double = 0.10
    ): Result<WithdrawalEntity> = withContext(Dispatchers.IO) {
        val user = dao.getUser(currentUserId) ?: return@withContext Result.failure(Exception("Utilisateur non trouvé"))
        val config = dao.getPlatformConfig() ?: PlatformConfigEntity()

        if (user.isFraudFlagged) {
            return@withContext Result.failure(Exception("Compte sous investigation. Les retraits sont temporairement suspendus."))
        }

        if (amountUsd < config.minWithdrawalUsd) {
            return@withContext Result.failure(Exception("Le montant minimum de retrait est de ${config.minWithdrawalUsd} USD (${(config.minWithdrawalUsd * rateUsdToCdf).toInt()} CDF)."))
        }

        if (amountUsd > user.balanceUsd) {
            return@withContext Result.failure(Exception("Solde insuffisant. Votre solde disponible est de ${String.format("%.2f", user.balanceUsd)} USD."))
        }

        val feeUsd = (amountUsd * (feePercent / 100.0)) + fixedFeeUsd
        val netReceivedUsd = (amountUsd - feeUsd).coerceAtLeast(0.0)
        val amountCdf = amountUsd * rateUsdToCdf
        val netReceivedCdf = netReceivedUsd * rateUsdToCdf

        val withdrawalId = "WTH-" + System.currentTimeMillis().toString().takeLast(6)
        val withdrawal = WithdrawalEntity(
            id = withdrawalId,
            userId = user.id,
            amountUsd = amountUsd,
            exchangeRateUsed = rateUsdToCdf,
            amountCdf = amountCdf,
            feeUsd = feeUsd,
            netReceivedUsd = netReceivedUsd,
            netReceivedCdf = netReceivedCdf,
            paymentMethodCode = paymentMethodCode,
            recipientAccount = recipientAccount,
            recipientName = recipientName,
            status = WithdrawalStatus.PENDING.name,
            transactionRef = "PENDING_GATEWAY_DISPATCH"
        )

        // Deduct balance securely
        val updatedUser = user.copy(balanceUsd = user.balanceUsd - amountUsd)
        dao.updateUser(updatedUser)
        dao.insertWithdrawal(withdrawal)

        // Record transaction
        dao.insertTransaction(
            TransactionEntity(
                id = "tx_" + UUID.randomUUID().toString().take(8),
                userId = user.id,
                title = "Demande de retrait ($paymentMethodCode vers $recipientAccount)",
                type = TransactionType.WITHDRAWAL.name,
                amountUsd = amountUsd,
                isCredit = false,
                note = "En cours de traitement sécurisé"
            )
        )

        Result.success(withdrawal)
    }

    // Admin Operations
    suspend fun adminApproveWithdrawal(withdrawalId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        val withdrawal = dao.getWithdrawalById(withdrawalId) ?: return@withContext Result.failure(Exception("Retrait introuvable"))
        val updated = withdrawal.copy(
            status = WithdrawalStatus.PAID.name,
            transactionRef = "TX-MPESA-" + System.currentTimeMillis().toString().takeLast(8),
            gatewayResponse = "Paiement exécuté avec succès via passerelle partenaire"
        )
        dao.updateWithdrawal(updated)
        Result.success(true)
    }

    suspend fun adminRejectWithdrawal(withdrawalId: String, reason: String): Result<Boolean> = withContext(Dispatchers.IO) {
        val withdrawal = dao.getWithdrawalById(withdrawalId) ?: return@withContext Result.failure(Exception("Retrait introuvable"))
        val user = dao.getUser(withdrawal.userId) ?: return@withContext Result.failure(Exception("Utilisateur introuvable"))

        val updated = withdrawal.copy(
            status = WithdrawalStatus.REJECTED.name,
            rejectionReason = reason,
            gatewayResponse = "Rejeté par l'administrateur: $reason"
        )
        dao.updateWithdrawal(updated)

        // Refund the amount back to user's balance
        val refundedUser = user.copy(balanceUsd = user.balanceUsd + withdrawal.amountUsd)
        dao.updateUser(refundedUser)

        dao.insertTransaction(
            TransactionEntity(
                id = "tx_" + UUID.randomUUID().toString().take(8),
                userId = user.id,
                title = "Remboursement retrait annulé (#$withdrawalId)",
                type = TransactionType.REFUND.name,
                amountUsd = withdrawal.amountUsd,
                isCredit = true,
                note = reason
            )
        )
        Result.success(true)
    }

    suspend fun adminApproveSubmission(submissionId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        val ok = creditSubmissionReward(submissionId)
        if (ok) Result.success(true) else Result.failure(Exception("Échec de la validation de la soumission"))
    }

    suspend fun adminRejectSubmission(submissionId: String, reason: String): Result<Boolean> = withContext(Dispatchers.IO) {
        val submission = dao.getSubmissionById(submissionId) ?: return@withContext Result.failure(Exception("Soumission introuvable"))
        val updated = submission.copy(
            status = SubmissionStatus.REJECTED.name,
            reviewNotes = reason
        )
        dao.updateSubmission(updated)
        Result.success(true)
    }

    suspend fun adminAddTask(task: TaskItemEntity): Result<Boolean> = withContext(Dispatchers.IO) {
        dao.insertTask(task)
        Result.success(true)
    }

    suspend fun adminToggleUserFraudFlag(userId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        val user = dao.getUser(userId) ?: return@withContext Result.failure(Exception("Utilisateur introuvable"))
        val updated = user.copy(isFraudFlagged = !user.isFraudFlagged)
        dao.updateUser(updated)
        Result.success(true)
    }

    suspend fun setCurrencyPreference(currency: String): Unit = withContext(Dispatchers.IO) {
        val user = dao.getUser(currentUserId) ?: return@withContext
        dao.updateUser(user.copy(currencyPreference = currency))
    }

    suspend fun setLanguagePreference(language: String): Unit = withContext(Dispatchers.IO) {
        val user = dao.getUser(currentUserId) ?: return@withContext
        dao.updateUser(user.copy(languagePreference = language))
    }

    suspend fun updateUserProfile(name: String, phone: String, country: String): Unit = withContext(Dispatchers.IO) {
        val user = dao.getUser(currentUserId) ?: return@withContext
        dao.updateUser(user.copy(name = name, phone = phone, country = country))
    }

    suspend fun claimReferralBonus(): Result<Double> = withContext(Dispatchers.IO) {
        val user = dao.getUser(currentUserId) ?: return@withContext Result.failure(Exception("Utilisateur non trouvé"))
        if (user.referralBonusAvailableUsd <= 0.0) {
            return@withContext Result.failure(Exception("Aucun bonus de parrainage en attente."))
        }
        val bonus = user.referralBonusAvailableUsd
        val updatedUser = user.copy(
            balanceUsd = user.balanceUsd + bonus,
            totalEarnedUsd = user.totalEarnedUsd + bonus,
            referralBonusAvailableUsd = 0.0
        )
        dao.updateUser(updatedUser)
        dao.insertTransaction(
            TransactionEntity(
                id = "tx_" + UUID.randomUUID().toString().take(8),
                userId = user.id,
                title = "Réclamation bonus de parrainage",
                type = TransactionType.REFERRAL_BONUS.name,
                amountUsd = bonus,
                isCredit = true
            )
        )
        Result.success(bonus)
    }
}
