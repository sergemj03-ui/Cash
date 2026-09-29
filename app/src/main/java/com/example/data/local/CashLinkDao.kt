package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.PlatformConfigEntity
import com.example.data.model.TaskItemEntity
import com.example.data.model.TaskSubmissionEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.UserEntity
import com.example.data.model.WithdrawalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CashLinkDao {

    // User Operations
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun getUserFlow(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUser(userId: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY level DESC")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    // Tasks Operations
    @Query("SELECT * FROM tasks WHERE isActive = 1 ORDER BY isFeatured DESC, createdAt DESC")
    fun getAllActiveTasksFlow(): Flow<List<TaskItemEntity>>

    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAllTasksFlow(): Flow<List<TaskItemEntity>>

    @Query("SELECT * FROM tasks WHERE id = :taskId LIMIT 1")
    suspend fun getTaskById(taskId: String): TaskItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskItemEntity>)

    @Update
    suspend fun updateTask(task: TaskItemEntity)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: String)

    // Submissions Operations
    @Query("SELECT * FROM task_submissions WHERE userId = :userId ORDER BY submittedAt DESC")
    fun getUserSubmissionsFlow(userId: String): Flow<List<TaskSubmissionEntity>>

    @Query("SELECT * FROM task_submissions ORDER BY submittedAt DESC")
    fun getAllSubmissionsFlow(): Flow<List<TaskSubmissionEntity>>

    @Query("SELECT * FROM task_submissions WHERE status = 'PENDING' OR status = 'IN_REVIEW' ORDER BY submittedAt ASC")
    fun getPendingSubmissionsFlow(): Flow<List<TaskSubmissionEntity>>

    @Query("SELECT * FROM task_submissions WHERE id = :submissionId LIMIT 1")
    suspend fun getSubmissionById(submissionId: String): TaskSubmissionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubmission(submission: TaskSubmissionEntity)

    @Update
    suspend fun updateSubmission(submission: TaskSubmissionEntity)

    // Withdrawals Operations
    @Query("SELECT * FROM withdrawals WHERE userId = :userId ORDER BY requestedAt DESC")
    fun getUserWithdrawalsFlow(userId: String): Flow<List<WithdrawalEntity>>

    @Query("SELECT * FROM withdrawals ORDER BY requestedAt DESC")
    fun getAllWithdrawalsFlow(): Flow<List<WithdrawalEntity>>

    @Query("SELECT * FROM withdrawals WHERE id = :withdrawalId LIMIT 1")
    suspend fun getWithdrawalById(withdrawalId: String): WithdrawalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWithdrawal(withdrawal: WithdrawalEntity)

    @Update
    suspend fun updateWithdrawal(withdrawal: WithdrawalEntity)

    // Transactions Operations
    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY timestamp DESC")
    fun getUserTransactionsFlow(userId: String): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    // Platform Config Operations
    @Query("SELECT * FROM platform_config WHERE id = 'default_config' LIMIT 1")
    fun getPlatformConfigFlow(): Flow<PlatformConfigEntity?>

    @Query("SELECT * FROM platform_config WHERE id = 'default_config' LIMIT 1")
    suspend fun getPlatformConfig(): PlatformConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlatformConfig(config: PlatformConfigEntity)

    @Update
    suspend fun updatePlatformConfig(config: PlatformConfigEntity)
}
