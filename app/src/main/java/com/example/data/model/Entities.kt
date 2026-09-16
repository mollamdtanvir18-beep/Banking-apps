package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "Md. Tanvir Mollah",
    val phone: String = "+880 1712-345678",
    val email: String = "mollamdtanvir18@gmail.com",
    val accountNumber: String = "TB-8920-4102-99",
    val balance: Double = 84500.00,
    val pendingBalance: Double = 5000.00,
    val pin: String = "1234",
    val isFingerprintEnabled: Boolean = true,
    val kycStatus: String = "VERIFIED", // "NOT_SUBMITTED", "PENDING", "VERIFIED", "REJECTED"
    val kycDocumentType: String = "National ID (NID)",
    val kycDocNumber: String = "1995829103859",
    val kycLegalName: String = "Md. Tanvir Mollah",
    val kycDateOfBirth: String = "14 Aug 1995",
    val currencySymbol: String = "৳",
    val isDarkTheme: Boolean = false,
    val isHideBalance: Boolean = false,
    val language: String = "English",
    val pushNotificationsEnabled: Boolean = true,
    val smsAlertsEnabled: Boolean = true,
    val maskAccountNumbers: Boolean = false
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val trxId: String,
    val type: String, // "DEPOSIT", "WITHDRAWAL", "SEND", "RECEIVE"
    val method: String, // "bKash", "Nagad", "Bank Transfer", "TrustBank Wallet"
    val amount: Double,
    val fee: Double = 0.0,
    val recipientOrSender: String,
    val status: String, // "SUCCESSFUL", "PENDING", "FAILED"
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = "",
    val referenceNumber: String = ""
)

@Entity(tableName = "deposit_requests")
data class DepositRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val trxId: String,
    val method: String, // "bKash", "Nagad", "Bank Transfer"
    val amount: Double,
    val paymentNumber: String,
    val transactionId: String,
    val status: String = "PENDING", // "PENDING", "APPROVED", "REJECTED"
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = "",
    val adminNote: String = ""
)

@Entity(tableName = "withdrawal_requests")
data class WithdrawalRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val trxId: String,
    val method: String, // "bKash", "Nagad", "Bank Account"
    val amount: Double,
    val fee: Double,
    val targetAccount: String,
    val status: String = "PENDING", // "PENDING", "APPROVED", "REJECTED"
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = "",
    val adminNote: String = ""
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val message: String,
    val type: String, // "DEPOSIT", "WITHDRAWAL", "TRANSACTION", "PROMOTION", "SECURITY"
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "admin_config")
data class AdminConfigEntity(
    @PrimaryKey val id: Int = 1,
    val bkashNumber: String = "01888-999000 (Merchant)",
    val nagadNumber: String = "01999-888111 (Merchant)",
    val bankName: String = "Eastern Bank Ltd (EBL)",
    val bankAccountName: String = "TrustBank Fintech Ltd",
    val bankAccountNumber: String = "1041009827361",
    val bankRouting: String = "090271562",
    val depositFeePercent: Double = 0.0,
    val withdrawalFeePercent: Double = 1.5,
    val minDeposit: Double = 100.0,
    val maxDeposit: Double = 100000.0,
    val minWithdrawal: Double = 100.0,
    val maxWithdrawal: Double = 50000.0,
    val isAdminMode: Boolean = false
)

@Entity(tableName = "support_tickets")
data class SupportTicketEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subject: String,
    val category: String,
    val message: String,
    val status: String = "OPEN", // "OPEN", "RESOLVED"
    val timestamp: Long = System.currentTimeMillis(),
    val reply: String = ""
)

data class MonthlyTrendItem(
    val monthKey: String,          // e.g. "2026-09"
    val monthLabel: String,        // e.g. "Sep"
    val fullMonthName: String,     // e.g. "September 2026"
    val totalDeposits: Double,
    val totalWithdrawals: Double,
    val depositCount: Int = 0,
    val withdrawalCount: Int = 0,
    val netCashflow: Double = totalDeposits - totalWithdrawals
)
