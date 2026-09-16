package com.example.data.repository

import com.example.data.db.BankingDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

class BankingRepository(private val dao: BankingDao) {

    val userProfile: Flow<UserProfile?> = dao.getUserProfile()
    val allTransactions: Flow<List<TransactionEntity>> = dao.getAllTransactions()
    val allDepositRequests: Flow<List<DepositRequestEntity>> = dao.getAllDepositRequests()
    val allWithdrawalRequests: Flow<List<WithdrawalRequestEntity>> = dao.getAllWithdrawalRequests()
    val allNotifications: Flow<List<NotificationEntity>> = dao.getAllNotifications()
    val adminConfig: Flow<AdminConfigEntity?> = dao.getAdminConfig()
    val allTickets: Flow<List<SupportTicketEntity>> = dao.getAllTickets()

    private fun generateTrxId(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        val randomStr = (1..8).map { chars.random() }.joinToString("")
        return "TRX$randomStr"
    }

    suspend fun getUserProfileOnce(): UserProfile? = dao.getUserProfileOnce()

    suspend fun updateProfile(name: String, email: String, phone: String) {
        val current = dao.getUserProfileOnce() ?: return
        dao.insertOrUpdateProfile(current.copy(name = name, email = email, phone = phone))
    }

    suspend fun changePin(oldPin: String, newPin: String): Boolean {
        val current = dao.getUserProfileOnce() ?: return false
        if (current.pin == oldPin) {
            dao.updatePin(newPin)
            dao.insertNotification(
                NotificationEntity(
                    title = "Security PIN Changed",
                    message = "Your banking transaction PIN was successfully updated.",
                    type = "SECURITY"
                )
            )
            return true
        }
        return false
    }

    suspend fun toggleFingerprint(enabled: Boolean) {
        dao.updateFingerprint(enabled)
    }

    suspend fun toggleHideBalance(hide: Boolean) {
        dao.updateHideBalance(hide)
    }

    suspend fun updateTheme(isDark: Boolean) {
        dao.updateTheme(isDark)
    }

    suspend fun updateLanguage(lang: String) {
        dao.updateLanguage(lang)
    }

    suspend fun submitDeposit(
        method: String,
        amount: Double,
        senderNumber: String,
        transactionId: String,
        notes: String = ""
    ): String {
        val trxId = generateTrxId()
        val deposit = DepositRequestEntity(
            trxId = trxId,
            method = method,
            amount = amount,
            paymentNumber = senderNumber,
            transactionId = transactionId,
            status = "PENDING",
            notes = notes
        )
        dao.insertDepositRequest(deposit)

        dao.insertTransaction(
            TransactionEntity(
                trxId = trxId,
                type = "DEPOSIT",
                method = method,
                amount = amount,
                fee = 0.0,
                recipientOrSender = "$method: $senderNumber",
                status = "PENDING",
                note = "Deposit via $method (Trx: $transactionId)",
                referenceNumber = transactionId
            )
        )

        // Add to pending balance
        val current = dao.getUserProfileOnce()
        if (current != null) {
            dao.updatePendingBalance(current.pendingBalance + amount)
        }

        dao.insertNotification(
            NotificationEntity(
                title = "Deposit Request Submitted",
                message = "Your deposit of ৳$amount via $method (TrxID: $transactionId) is under review.",
                type = "DEPOSIT"
            )
        )
        return trxId
    }

    suspend fun submitWithdrawal(
        method: String,
        amount: Double,
        fee: Double,
        targetAccount: String,
        notes: String = ""
    ): Result<String> {
        val current = dao.getUserProfileOnce() ?: return Result.failure(Exception("User not found"))
        val totalDebit = amount + fee
        if (current.balance < totalDebit) {
            return Result.failure(Exception("Insufficient balance. Total required: ৳$totalDebit"))
        }

        val trxId = generateTrxId()
        dao.debitBalance(totalDebit)

        dao.insertWithdrawalRequest(
            WithdrawalRequestEntity(
                trxId = trxId,
                method = method,
                amount = amount,
                fee = fee,
                targetAccount = targetAccount,
                status = "PENDING",
                notes = notes
            )
        )

        dao.insertTransaction(
            TransactionEntity(
                trxId = trxId,
                type = "WITHDRAWAL",
                method = method,
                amount = amount,
                fee = fee,
                recipientOrSender = "$method: $targetAccount",
                status = "PENDING",
                note = "Withdrawal request to $targetAccount"
            )
        )

        dao.insertNotification(
            NotificationEntity(
                title = "Withdrawal Initiated",
                message = "Withdrawal request for ৳$amount to $targetAccount is processing. Fee: ৳$fee.",
                type = "WITHDRAWAL"
            )
        )

        return Result.success(trxId)
    }

    suspend fun sendMoney(
        recipient: String,
        amount: Double,
        note: String
    ): Result<String> {
        val current = dao.getUserProfileOnce() ?: return Result.failure(Exception("User not found"))
        val fee = 5.0 // Flat Send Money charge
        val totalDebit = amount + fee
        if (current.balance < totalDebit) {
            return Result.failure(Exception("Insufficient funds. You need ৳$totalDebit (includes ৳$fee fee)"))
        }

        val trxId = generateTrxId()
        dao.debitBalance(totalDebit)

        dao.insertTransaction(
            TransactionEntity(
                trxId = trxId,
                type = "SEND",
                method = "TrustBank Wallet",
                amount = amount,
                fee = fee,
                recipientOrSender = recipient,
                status = "SUCCESSFUL",
                note = if (note.isNotBlank()) note else "Direct money transfer"
            )
        )

        dao.insertNotification(
            NotificationEntity(
                title = "Money Sent Successfully",
                message = "৳$amount sent to $recipient. Fee: ৳$fee. TrxID: $trxId.",
                type = "TRANSACTION"
            )
        )

        return Result.success(trxId)
    }

    suspend fun submitKyc(
        docType: String,
        docNumber: String,
        legalName: String,
        dob: String
    ) {
        dao.submitKyc(
            status = "PENDING",
            docType = docType,
            docNumber = docNumber,
            legalName = legalName,
            dob = dob
        )
        dao.insertNotification(
            NotificationEntity(
                title = "KYC Documents Submitted",
                message = "Your $docType ($docNumber) has been received and is being verified by the compliance team.",
                type = "SECURITY"
            )
        )
    }

    // Admin Operations
    suspend fun adminApproveDeposit(depositId: Long) {
        val depositRequests = dao.getAllDepositRequests()
        // We update the request
        dao.updateDepositStatus(depositId, "APPROVED", "Verified and approved by admin")

        // In real app we lookup by deposit id
        val current = dao.getUserProfileOnce() ?: return
        // Find corresponding transaction & amount
        // For local simplicity, we query deposit requests:
        // We credit the balance and decrease pending
        // Credit the balance
        // We'll update through dao helper
    }

    suspend fun adminApproveDepositRequest(deposit: DepositRequestEntity) {
        dao.updateDepositStatus(deposit.id, "APPROVED", "Verified and approved by Admin")
        val current = dao.getUserProfileOnce() ?: return
        val newPending = (current.pendingBalance - deposit.amount).coerceAtLeast(0.0)
        dao.updatePendingBalance(newPending)
        dao.creditBalance(deposit.amount)

        // Insert / Update transaction record to SUCCESSFUL
        dao.insertTransaction(
            TransactionEntity(
                trxId = deposit.trxId,
                type = "DEPOSIT",
                method = deposit.method,
                amount = deposit.amount,
                fee = 0.0,
                recipientOrSender = "${deposit.method}: ${deposit.paymentNumber}",
                status = "SUCCESSFUL",
                note = "Deposit Approved: ${deposit.notes}"
            )
        )

        dao.insertNotification(
            NotificationEntity(
                title = "Deposit Approved! 🎉",
                message = "৳${deposit.amount} has been added to your TrustBank balance. TrxID: ${deposit.trxId}",
                type = "DEPOSIT"
            )
        )
    }

    suspend fun adminRejectDepositRequest(deposit: DepositRequestEntity, reason: String) {
        dao.updateDepositStatus(deposit.id, "REJECTED", reason)
        val current = dao.getUserProfileOnce() ?: return
        val newPending = (current.pendingBalance - deposit.amount).coerceAtLeast(0.0)
        dao.updatePendingBalance(newPending)

        dao.insertTransaction(
            TransactionEntity(
                trxId = deposit.trxId,
                type = "DEPOSIT",
                method = deposit.method,
                amount = deposit.amount,
                fee = 0.0,
                recipientOrSender = "${deposit.method}: ${deposit.paymentNumber}",
                status = "FAILED",
                note = "Deposit Rejected: $reason"
            )
        )

        dao.insertNotification(
            NotificationEntity(
                title = "Deposit Rejected",
                message = "Your deposit of ৳${deposit.amount} was rejected. Reason: $reason",
                type = "DEPOSIT"
            )
        )
    }

    suspend fun adminApproveWithdrawalRequest(withdrawal: WithdrawalRequestEntity) {
        dao.updateWithdrawalStatus(withdrawal.id, "APPROVED", "Paid out by Admin")
        dao.insertTransaction(
            TransactionEntity(
                trxId = withdrawal.trxId,
                type = "WITHDRAWAL",
                method = withdrawal.method,
                amount = withdrawal.amount,
                fee = withdrawal.fee,
                recipientOrSender = "${withdrawal.method}: ${withdrawal.targetAccount}",
                status = "SUCCESSFUL",
                note = "Withdrawal Dispatched via ${withdrawal.method}"
            )
        )
        dao.insertNotification(
            NotificationEntity(
                title = "Withdrawal Completed 💸",
                message = "Your withdrawal of ৳${withdrawal.amount} to ${withdrawal.targetAccount} has been completed.",
                type = "WITHDRAWAL"
            )
        )
    }

    suspend fun adminRejectWithdrawalRequest(withdrawal: WithdrawalRequestEntity, reason: String) {
        dao.updateWithdrawalStatus(withdrawal.id, "REJECTED", reason)
        // Refund balance + fee
        val refundAmount = withdrawal.amount + withdrawal.fee
        dao.creditBalance(refundAmount)

        dao.insertTransaction(
            TransactionEntity(
                trxId = withdrawal.trxId,
                type = "WITHDRAWAL",
                method = withdrawal.method,
                amount = withdrawal.amount,
                fee = withdrawal.fee,
                recipientOrSender = "${withdrawal.method}: ${withdrawal.targetAccount}",
                status = "FAILED",
                note = "Withdrawal Rejected & Refunded: $reason"
            )
        )

        dao.insertNotification(
            NotificationEntity(
                title = "Withdrawal Refunded",
                message = "Your withdrawal of ৳${withdrawal.amount} was rejected and ৳$refundAmount has been refunded to your wallet.",
                type = "WITHDRAWAL"
            )
        )
    }

    suspend fun adminUpdateKycStatus(status: String) {
        dao.updateKycStatus(status)
        val msg = if (status == "VERIFIED") {
            "Congratulations! Your KYC profile has been verified. All account limits are now unlocked."
        } else {
            "Your KYC submission was rejected. Please re-check your documents and re-apply."
        }
        dao.insertNotification(
            NotificationEntity(
                title = if (status == "VERIFIED") "KYC Approved ✅" else "KYC Rejected ⚠️",
                message = msg,
                type = "SECURITY"
            )
        )
    }

    suspend fun adminAdjustBalance(amount: Double, isCredit: Boolean, note: String) {
        if (isCredit) {
            dao.creditBalance(amount)
        } else {
            dao.debitBalance(amount)
        }
        val trxId = generateTrxId()
        dao.insertTransaction(
            TransactionEntity(
                trxId = trxId,
                type = if (isCredit) "DEPOSIT" else "WITHDRAWAL",
                method = "Admin Adjustment",
                amount = amount,
                fee = 0.0,
                recipientOrSender = "System Admin",
                status = "SUCCESSFUL",
                note = note.ifBlank { "Administrative balance adjustment" }
            )
        )
        dao.insertNotification(
            NotificationEntity(
                title = if (isCredit) "Account Credited by Admin" else "Account Debited by Admin",
                message = "৳$amount was ${if (isCredit) "credited to" else "debited from"} your wallet. Note: $note",
                type = "TRANSACTION"
            )
        )
    }

    suspend fun updateAdminConfig(config: AdminConfigEntity) {
        dao.insertOrUpdateAdminConfig(config)
    }

    suspend fun sendBroadcastNotification(title: String, message: String, type: String) {
        dao.insertNotification(
            NotificationEntity(
                title = title,
                message = message,
                type = type
            )
        )
    }

    suspend fun markNotificationAsRead(id: Long) {
        dao.markNotificationAsRead(id)
    }

    suspend fun markAllNotificationsAsRead() {
        dao.markAllNotificationsAsRead()
    }

    suspend fun clearAllNotifications() {
        dao.clearAllNotifications()
    }

    suspend fun submitSupportTicket(subject: String, category: String, message: String) {
        val ticketId = dao.insertTicket(
            SupportTicketEntity(
                subject = subject,
                category = category,
                message = message,
                status = "OPEN"
            )
        )
        // Auto bot response
        val botReply = "Thank you for reaching out! Our senior banking officer is reviewing ticket #TB-$ticketId. Expected response time: under 15 minutes."
        dao.resolveTicket(ticketId, "RESOLVED", botReply)
    }
}
