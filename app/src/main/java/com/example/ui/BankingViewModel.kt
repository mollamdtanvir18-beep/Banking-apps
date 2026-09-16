package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.*
import com.example.data.repository.BankingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class ChatMessage(
    val id: Long = System.currentTimeMillis(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

enum class NavigationTab {
    HOME,
    WALLET,
    TRANSACTIONS,
    SUPPORT,
    PROFILE,
    ADMIN
}

class BankingViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BankingRepository

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = BankingRepository(database.bankingDao())
    }

    val userProfile: StateFlow<UserProfile> = repository.userProfile
        .filterNotNull()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserProfile()
        )

    val transactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val depositRequests: StateFlow<List<DepositRequestEntity>> = repository.allDepositRequests
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val withdrawalRequests: StateFlow<List<WithdrawalRequestEntity>> = repository.allWithdrawalRequests
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val notifications: StateFlow<List<NotificationEntity>> = repository.allNotifications
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val adminConfig: StateFlow<AdminConfigEntity> = repository.adminConfig
        .filterNotNull()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AdminConfigEntity()
        )

    val supportTickets: StateFlow<List<SupportTicketEntity>> = repository.allTickets
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val monthlyTrends: StateFlow<List<MonthlyTrendItem>> = transactions.map { trxList ->
        computeMonthlyTrends(trxList)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private fun computeMonthlyTrends(trxList: List<TransactionEntity>): List<MonthlyTrendItem> {
        val result = mutableListOf<MonthlyTrendItem>()
        val monthKeys = mutableListOf<Triple<String, String, String>>()

        val keyFormat = SimpleDateFormat("yyyy-MM", Locale.US)
        val labelFormat = SimpleDateFormat("MMM", Locale.US)
        val fullFormat = SimpleDateFormat("MMMM yyyy", Locale.US)

        for (i in 5 downTo 0) {
            val c = Calendar.getInstance()
            c.add(Calendar.MONTH, -i)
            val key = keyFormat.format(c.time)
            val label = labelFormat.format(c.time)
            val full = fullFormat.format(c.time)
            monthKeys.add(Triple(key, label, full))
        }

        val successfulTrx = trxList.filter { it.status == "SUCCESSFUL" }

        for ((key, label, full) in monthKeys) {
            val inMonth = successfulTrx.filter { keyFormat.format(Date(it.timestamp)) == key }
            val deposits = inMonth.filter { it.type == "DEPOSIT" || it.type == "RECEIVE" }
            val withdrawals = inMonth.filter { it.type == "WITHDRAWAL" || it.type == "SEND" }

            val totalDeposits = deposits.sumOf { it.amount }
            val totalWithdrawals = withdrawals.sumOf { it.amount }

            result.add(
                MonthlyTrendItem(
                    monthKey = key,
                    monthLabel = label,
                    fullMonthName = full,
                    totalDeposits = totalDeposits,
                    totalWithdrawals = totalWithdrawals,
                    depositCount = deposits.size,
                    withdrawalCount = withdrawals.size,
                    netCashflow = totalDeposits - totalWithdrawals
                )
            )
        }
        return result
    }

    // Navigation & UI States
    private val _currentTab = MutableStateFlow(NavigationTab.HOME)
    val currentTab: StateFlow<NavigationTab> = _currentTab.asStateFlow()

    private val _isAdminMode = MutableStateFlow(false)
    val isAdminMode: StateFlow<Boolean> = _isAdminMode.asStateFlow()

    // Filter states for Transactions tab
    private val _transactionTypeFilter = MutableStateFlow("ALL")
    val transactionTypeFilter: StateFlow<String> = _transactionTypeFilter.asStateFlow()

    private val _transactionStatusFilter = MutableStateFlow("ALL")
    val transactionStatusFilter: StateFlow<String> = _transactionStatusFilter.asStateFlow()

    private val _transactionSearchQuery = MutableStateFlow("")
    val transactionSearchQuery: StateFlow<String> = _transactionSearchQuery.asStateFlow()

    // Dialog & Sheet States
    val showAddMoneySheet = MutableStateFlow(false)
    val showWithdrawSheet = MutableStateFlow(false)
    val showSendMoneySheet = MutableStateFlow(false)
    val showReceiveMoneySheet = MutableStateFlow(false)
    val showNotificationsSheet = MutableStateFlow(false)
    val showKycSheet = MutableStateFlow(false)
    val showSecuritySheet = MutableStateFlow(false)
    val showSettingsSheet = MutableStateFlow(false)
    val showEditProfileDialog = MutableStateFlow(false)
    val showChangePinDialog = MutableStateFlow(false)
    val showLiveChatDialog = MutableStateFlow(false)
    val showReportProblemDialog = MutableStateFlow(false)
    val selectedTransaction = MutableStateFlow<TransactionEntity?>(null)

    // SnackBar / Alert message
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    // Live Chat State
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                text = "Hello! 👋 Welcome to TrustBank 24/7 Priority Support. I can help you with balance queries, deposit instructions, withdrawal status, or account limits. How can I help you today?",
                isUser = false
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    fun selectTab(tab: NavigationTab) {
        _currentTab.value = tab
    }

    fun toggleAdminMode(enabled: Boolean) {
        _isAdminMode.value = enabled
        if (enabled) {
            _currentTab.value = NavigationTab.ADMIN
        } else {
            _currentTab.value = NavigationTab.HOME
        }
    }

    fun setTransactionFilters(type: String? = null, status: String? = null, query: String? = null) {
        type?.let { _transactionTypeFilter.value = it }
        status?.let { _transactionStatusFilter.value = it }
        query?.let { _transactionSearchQuery.value = it }
    }

    fun toggleHideBalance() {
        viewModelScope.launch(Dispatchers.IO) {
            val current = userProfile.value.isHideBalance
            repository.toggleHideBalance(!current)
        }
    }

    fun toggleFingerprint(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleFingerprint(enabled)
            showSnackbar(if (enabled) "Biometric Fingerprint login enabled" else "Biometric login disabled")
        }
    }

    fun updateProfile(name: String, email: String, phone: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateProfile(name, email, phone)
            showSnackbar("Profile updated successfully")
            showEditProfileDialog.value = false
        }
    }

    fun changePin(oldPin: String, newPin: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = repository.changePin(oldPin, newPin)
            if (success) {
                showSnackbar("PIN changed successfully")
                onSuccess()
            } else {
                onError("Incorrect current PIN. Please try again.")
            }
        }
    }

    fun submitDeposit(
        method: String,
        amount: Double,
        senderNumber: String,
        trxId: String,
        notes: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val generatedTrx = repository.submitDeposit(method, amount, senderNumber, trxId, notes)
            showSnackbar("Deposit request submitted! TrxID: $generatedTrx")
            showAddMoneySheet.value = false
        }
    }

    fun submitWithdrawal(
        method: String,
        amount: Double,
        targetAccount: String,
        pin: String,
        notes: String,
        onComplete: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = userProfile.value
            if (current.pin != pin) {
                onComplete(false, "Invalid transaction PIN.")
                return@launch
            }
            val feePercent = adminConfig.value.withdrawalFeePercent
            val fee = (amount * feePercent / 100.0)
            val result = repository.submitWithdrawal(method, amount, fee, targetAccount, notes)
            result.onSuccess { trx ->
                showSnackbar("Withdrawal request for ৳$amount submitted!")
                showWithdrawSheet.value = false
                onComplete(true, trx)
            }.onFailure { err ->
                onComplete(false, err.message ?: "Failed to process withdrawal")
            }
        }
    }

    fun sendMoney(
        recipient: String,
        amount: Double,
        note: String,
        pin: String,
        onComplete: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = userProfile.value
            if (current.pin != pin) {
                onComplete(false, "Invalid transaction PIN.")
                return@launch
            }
            val result = repository.sendMoney(recipient, amount, note)
            result.onSuccess { trx ->
                showSnackbar("৳$amount sent to $recipient successfully!")
                showSendMoneySheet.value = false
                onComplete(true, trx)
            }.onFailure { err ->
                onComplete(false, err.message ?: "Transfer failed")
            }
        }
    }

    fun submitKyc(docType: String, docNumber: String, legalName: String, dob: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.submitKyc(docType, docNumber, legalName, dob)
            showSnackbar("KYC Verification submitted for compliance review")
            showKycSheet.value = false
        }
    }

    fun submitSupportTicket(subject: String, category: String, message: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.submitSupportTicket(subject, category, message)
            showSnackbar("Support ticket submitted. Check ticket log.")
            showReportProblemDialog.value = false
        }
    }

    fun sendChatMessage(userText: String) {
        if (userText.isBlank()) return
        val userMsg = ChatMessage(text = userText, isUser = true)
        _chatMessages.value = _chatMessages.value + userMsg

        viewModelScope.launch {
            delay(800)
            val query = userText.lowercase()
            val profile = userProfile.value
            val config = adminConfig.value

            val reply = when {
                "balance" in query -> {
                    "Your current Available Balance is ৳${String.format("%,.2f", profile.balance)}. Pending Balance: ৳${String.format("%,.2f", profile.pendingBalance)}."
                }
                "deposit" in query || "add money" in query || "bkash" in query || "nagad" in query -> {
                    "To add money, tap 'Add Money' on the Home screen. Select bKash, Nagad, or Bank Transfer. Current deposit fee is ${config.depositFeePercent}%."
                }
                "withdraw" in query || "cashout" in query -> {
                    "Withdrawals are processed to bKash (${config.bkashNumber}), Nagad (${config.nagadNumber}), or Bank Accounts. Withdrawal fee is ${config.withdrawalFeePercent}%."
                }
                "limit" in query -> {
                    "Daily limits: Deposit minimum ৳${config.minDeposit}, maximum ৳${config.maxDeposit}. Withdrawal minimum ৳${config.minWithdrawal}, maximum ৳${config.maxWithdrawal}."
                }
                "kyc" in query || "nid" in query || "verify" in query -> {
                    "Your KYC status is currently: ${profile.kycStatus}. You can submit your NID or Passport in the Profile tab."
                }
                "pin" in query || "security" in query -> {
                    "You can change your 4-digit PIN in Profile -> Change PIN. Default PIN is 1234."
                }
                "agent" in query || "human" in query -> {
                    "A human banking specialist will connect with you shortly. You can also call our 24/7 hotline at 16299 or email support@trustbank.com."
                }
                else -> {
                    "Thank you for reaching out! I've logged your request regarding '$userText'. Is there anything specific regarding deposits, transfers, or security I can assist with?"
                }
            }
            _chatMessages.value = _chatMessages.value + ChatMessage(text = reply, isUser = false)
        }
    }

    // Admin Operations
    fun adminApproveDeposit(deposit: DepositRequestEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.adminApproveDepositRequest(deposit)
            showSnackbar("Approved deposit of ৳${deposit.amount} (${deposit.method})")
        }
    }

    fun adminRejectDeposit(deposit: DepositRequestEntity, reason: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.adminRejectDepositRequest(deposit, reason)
            showSnackbar("Deposit request rejected.")
        }
    }

    fun adminApproveWithdrawal(withdrawal: WithdrawalRequestEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.adminApproveWithdrawalRequest(withdrawal)
            showSnackbar("Approved withdrawal of ৳${withdrawal.amount} to ${withdrawal.targetAccount}")
        }
    }

    fun adminRejectWithdrawal(withdrawal: WithdrawalRequestEntity, reason: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.adminRejectWithdrawalRequest(withdrawal, reason)
            showSnackbar("Withdrawal rejected and funds refunded to user.")
        }
    }

    fun adminUpdateKyc(status: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.adminUpdateKycStatus(status)
            showSnackbar("User KYC status updated to: $status")
        }
    }

    fun adminAdjustBalance(amount: Double, isCredit: Boolean, note: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.adminAdjustBalance(amount, isCredit, note)
            showSnackbar("Account balance ${if (isCredit) "credited" else "debited"} by ৳$amount")
        }
    }

    fun adminUpdateConfig(config: AdminConfigEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateAdminConfig(config)
            showSnackbar("System banking configuration saved!")
        }
    }

    fun adminBroadcastNotification(title: String, message: String, type: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.sendBroadcastNotification(title, message, type)
            showSnackbar("Notification broadcasted to all users!")
        }
    }

    fun markNotificationAsRead(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.markNotificationAsRead(id)
        }
    }

    fun markAllNotificationsAsRead() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.markAllNotificationsAsRead()
            showSnackbar("All notifications marked as read")
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAllNotifications()
            showSnackbar("Notification history cleared")
        }
    }

    fun showSnackbar(message: String) {
        _snackbarMessage.value = message
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    // Admin helpers matching UI calls
    fun approveDeposit(id: Long) {
        val req = depositRequests.value.find { it.id == id }
        if (req != null) {
            adminApproveDeposit(req)
        }
    }

    fun rejectDeposit(id: Long, reason: String = "Rejected by admin") {
        val req = depositRequests.value.find { it.id == id }
        if (req != null) {
            adminRejectDeposit(req, reason)
        }
    }

    fun approveWithdrawal(id: Long) {
        val req = withdrawalRequests.value.find { it.id == id }
        if (req != null) {
            adminApproveWithdrawal(req)
        }
    }

    fun rejectWithdrawal(id: Long, reason: String = "Rejected by admin") {
        val req = withdrawalRequests.value.find { it.id == id }
        if (req != null) {
            adminRejectWithdrawal(req, reason)
        }
    }

    fun updateKycStatus(status: String) {
        adminUpdateKyc(status)
    }

    fun adjustUserBalance(amount: Double, isCredit: Boolean, note: String) {
        adminAdjustBalance(amount, isCredit, note)
    }

    fun updateGateways(bkash: String, nagad: String, bankAccount: String) {
        val current = adminConfig.value
        adminUpdateConfig(current.copy(
            bkashNumber = bkash,
            nagadNumber = nagad,
            bankAccountNumber = bankAccount
        ))
    }

    fun updateFeesAndLimits(fee: Double, minDep: Double, maxDep: Double, minWith: Double, maxWith: Double) {
        val current = adminConfig.value
        adminUpdateConfig(current.copy(
            withdrawalFeePercent = fee,
            minDeposit = minDep,
            maxDeposit = maxDep,
            minWithdrawal = minWith,
            maxWithdrawal = maxWith
        ))
    }

    fun sendBroadcastNotification(title: String, message: String, type: String) {
        adminBroadcastNotification(title, message, type)
    }
}

class BankingViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BankingViewModel::class.java)) {
            return BankingViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

