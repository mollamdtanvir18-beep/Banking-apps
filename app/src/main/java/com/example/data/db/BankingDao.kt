package com.example.data.db

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BankingDao {

    // User Profile
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getUserProfileOnce(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfile)

    @Query("UPDATE user_profile SET balance = :newBalance WHERE id = 1")
    suspend fun updateBalance(newBalance: Double)

    @Query("UPDATE user_profile SET pendingBalance = :newPending WHERE id = 1")
    suspend fun updatePendingBalance(newPending: Double)

    @Query("UPDATE user_profile SET balance = balance + :amount WHERE id = 1")
    suspend fun creditBalance(amount: Double)

    @Query("UPDATE user_profile SET balance = balance - :amount WHERE id = 1")
    suspend fun debitBalance(amount: Double)

    @Query("UPDATE user_profile SET pin = :newPin WHERE id = 1")
    suspend fun updatePin(newPin: String)

    @Query("UPDATE user_profile SET isFingerprintEnabled = :enabled WHERE id = 1")
    suspend fun updateFingerprint(enabled: Boolean)

    @Query("UPDATE user_profile SET isHideBalance = :hide WHERE id = 1")
    suspend fun updateHideBalance(hide: Boolean)

    @Query("UPDATE user_profile SET kycStatus = :status, kycDocumentType = :docType, kycDocNumber = :docNumber, kycLegalName = :legalName, kycDateOfBirth = :dob WHERE id = 1")
    suspend fun submitKyc(status: String, docType: String, docNumber: String, legalName: String, dob: String)

    @Query("UPDATE user_profile SET kycStatus = :status WHERE id = 1")
    suspend fun updateKycStatus(status: String)

    @Query("UPDATE user_profile SET isDarkTheme = :isDark WHERE id = 1")
    suspend fun updateTheme(isDark: Boolean)

    @Query("UPDATE user_profile SET language = :lang WHERE id = 1")
    suspend fun updateLanguage(lang: String)

    // Transactions
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY timestamp DESC")
    fun getTransactionsByType(type: String): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    // Deposit Requests
    @Query("SELECT * FROM deposit_requests ORDER BY timestamp DESC")
    fun getAllDepositRequests(): Flow<List<DepositRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDepositRequest(request: DepositRequestEntity): Long

    @Update
    suspend fun updateDepositRequest(request: DepositRequestEntity)

    @Query("UPDATE deposit_requests SET status = :status, adminNote = :note WHERE id = :id")
    suspend fun updateDepositStatus(id: Long, status: String, note: String)

    // Withdrawal Requests
    @Query("SELECT * FROM withdrawal_requests ORDER BY timestamp DESC")
    fun getAllWithdrawalRequests(): Flow<List<WithdrawalRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWithdrawalRequest(request: WithdrawalRequestEntity): Long

    @Update
    suspend fun updateWithdrawalRequest(request: WithdrawalRequestEntity)

    @Query("UPDATE withdrawal_requests SET status = :status, adminNote = :note WHERE id = :id")
    suspend fun updateWithdrawalStatus(id: Long, status: String, note: String)

    // Notifications
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity): Long

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationAsRead(id: Long)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllNotificationsAsRead()

    @Query("DELETE FROM notifications")
    suspend fun clearAllNotifications()

    // Admin Config
    @Query("SELECT * FROM admin_config WHERE id = 1")
    fun getAdminConfig(): Flow<AdminConfigEntity?>

    @Query("SELECT * FROM admin_config WHERE id = 1")
    suspend fun getAdminConfigOnce(): AdminConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAdminConfig(config: AdminConfigEntity)

    // Support Tickets
    @Query("SELECT * FROM support_tickets ORDER BY timestamp DESC")
    fun getAllTickets(): Flow<List<SupportTicketEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: SupportTicketEntity): Long

    @Query("UPDATE support_tickets SET status = :status, reply = :reply WHERE id = :id")
    suspend fun resolveTicket(id: Long, status: String, reply: String)
}
