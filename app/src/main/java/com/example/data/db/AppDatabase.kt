package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserProfile::class,
        TransactionEntity::class,
        DepositRequestEntity::class,
        WithdrawalRequestEntity::class,
        NotificationEntity::class,
        AdminConfigEntity::class,
        SupportTicketEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun bankingDao(): BankingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "trustbank_database"
                )
                .addCallback(BankingDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class BankingDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.bankingDao())
                    }
                }
            }

            suspend fun populateInitialData(dao: BankingDao) {
                // 1. Initial User
                dao.insertOrUpdateProfile(
                    UserProfile(
                        id = 1,
                        name = "Md. Tanvir Mollah",
                        phone = "+880 1712-345678",
                        email = "mollamdtanvir18@gmail.com",
                        accountNumber = "TB-8920-4102-99",
                        balance = 84500.00,
                        pendingBalance = 5000.00,
                        pin = "1234",
                        isFingerprintEnabled = true,
                        kycStatus = "VERIFIED",
                        kycDocumentType = "National ID (NID)",
                        kycDocNumber = "1995829103859",
                        kycLegalName = "Md. Tanvir Mollah",
                        kycDateOfBirth = "14 Aug 1995"
                    )
                )

                // 2. Initial Admin Config
                dao.insertOrUpdateAdminConfig(
                    AdminConfigEntity(
                        id = 1,
                        bkashNumber = "01888-999000 (Merchant)",
                        nagadNumber = "01999-888111 (Merchant)",
                        bankName = "Eastern Bank Ltd (EBL)",
                        bankAccountName = "TrustBank Fintech Ltd",
                        bankAccountNumber = "1041009827361",
                        bankRouting = "090271562",
                        depositFeePercent = 0.0,
                        withdrawalFeePercent = 1.5,
                        minDeposit = 100.0,
                        maxDeposit = 100000.0,
                        minWithdrawal = 100.0,
                        maxWithdrawal = 50000.0,
                        isAdminMode = false
                    )
                )

                // 3. Initial Sample Transactions with multi-month historical distribution
                val now = System.currentTimeMillis()
                val oneHourAgo = now - 3600_000L
                val oneDayAgo = now - 86400_000L
                val twoDaysAgo = now - 172800_000L
                val threeDaysAgo = now - 259200_000L
                val oneMonthAgo = now - (30L * 86400_000L)
                val twoMonthsAgo = now - (60L * 86400_000L)
                val threeMonthsAgo = now - (90L * 86400_000L)
                val fourMonthsAgo = now - (120L * 86400_000L)
                val fiveMonthsAgo = now - (150L * 86400_000L)

                dao.insertTransaction(
                    TransactionEntity(
                        trxId = "TRX93847291",
                        type = "DEPOSIT",
                        method = "bKash",
                        amount = 15000.00,
                        fee = 0.0,
                        recipientOrSender = "bKash: 01712-345678",
                        status = "SUCCESSFUL",
                        timestamp = now - 1800_000L,
                        note = "Wallet top-up via bKash Merchant"
                    )
                )

                dao.insertTransaction(
                    TransactionEntity(
                        trxId = "TRX84910283",
                        type = "SEND",
                        method = "TrustBank Wallet",
                        amount = 3500.00,
                        fee = 5.0,
                        recipientOrSender = "Farhan Ahmed (TB-5541-9021)",
                        status = "SUCCESSFUL",
                        timestamp = oneHourAgo,
                        note = "Monthly utility split"
                    )
                )

                dao.insertTransaction(
                    TransactionEntity(
                        trxId = "TRX77382910",
                        type = "WITHDRAWAL",
                        method = "Nagad",
                        amount = 5000.00,
                        fee = 75.0,
                        recipientOrSender = "Nagad: 01911-223344",
                        status = "SUCCESSFUL",
                        timestamp = oneDayAgo,
                        note = "Personal cashout"
                    )
                )

                dao.insertTransaction(
                    TransactionEntity(
                        trxId = "TRX66281934",
                        type = "RECEIVE",
                        method = "Bank Transfer",
                        amount = 45000.00,
                        fee = 0.0,
                        recipientOrSender = "City Bank Ltd (Salary A/C)",
                        status = "SUCCESSFUL",
                        timestamp = twoDaysAgo,
                        note = "Monthly salary disbursement"
                    )
                )

                dao.insertTransaction(
                    TransactionEntity(
                        trxId = "TRX55192837",
                        type = "DEPOSIT",
                        method = "Nagad",
                        amount = 5000.00,
                        fee = 0.0,
                        recipientOrSender = "Nagad: 01712-345678",
                        status = "PENDING",
                        timestamp = now - 900_000L,
                        note = "Deposit pending admin verification"
                    )
                )

                // Multi-month history for trends (1 month ago)
                dao.insertTransaction(
                    TransactionEntity(
                        trxId = "TRX44182901",
                        type = "DEPOSIT",
                        method = "bKash",
                        amount = 32000.00,
                        fee = 0.0,
                        recipientOrSender = "bKash: 01712-345678",
                        status = "SUCCESSFUL",
                        timestamp = oneMonthAgo + 86400_000L * 5,
                        note = "Project milestone payment"
                    )
                )
                dao.insertTransaction(
                    TransactionEntity(
                        trxId = "TRX44182902",
                        type = "WITHDRAWAL",
                        method = "Bank Transfer",
                        amount = 14500.00,
                        fee = 217.5,
                        recipientOrSender = "EBL A/C: 1041009827361",
                        status = "SUCCESSFUL",
                        timestamp = oneMonthAgo + 86400_000L * 12,
                        note = "House rent transfer"
                    )
                )

                // 2 months ago
                dao.insertTransaction(
                    TransactionEntity(
                        trxId = "TRX33192811",
                        type = "DEPOSIT",
                        method = "Bank Transfer",
                        amount = 48000.00,
                        fee = 0.0,
                        recipientOrSender = "Standard Chartered",
                        status = "SUCCESSFUL",
                        timestamp = twoMonthsAgo + 86400_000L * 4,
                        note = "Client consultation retainer"
                    )
                )
                dao.insertTransaction(
                    TransactionEntity(
                        trxId = "TRX33192812",
                        type = "WITHDRAWAL",
                        method = "bKash",
                        amount = 18000.00,
                        fee = 270.0,
                        recipientOrSender = "bKash: 01888-999000",
                        status = "SUCCESSFUL",
                        timestamp = twoMonthsAgo + 86400_000L * 15,
                        note = "Family emergency support"
                    )
                )

                // 3 months ago
                dao.insertTransaction(
                    TransactionEntity(
                        trxId = "TRX22193821",
                        type = "DEPOSIT",
                        method = "Nagad",
                        amount = 55000.00,
                        fee = 0.0,
                        recipientOrSender = "Nagad: 01999-888111",
                        status = "SUCCESSFUL",
                        timestamp = threeMonthsAgo + 86400_000L * 6,
                        note = "Quarterly performance bonus"
                    )
                )
                dao.insertTransaction(
                    TransactionEntity(
                        trxId = "TRX22193822",
                        type = "WITHDRAWAL",
                        method = "Nagad",
                        amount = 22500.00,
                        fee = 337.5,
                        recipientOrSender = "Nagad: 01911-223344",
                        status = "SUCCESSFUL",
                        timestamp = threeMonthsAgo + 86400_000L * 18,
                        note = "Laptop equipment purchase"
                    )
                )

                // 4 months ago
                dao.insertTransaction(
                    TransactionEntity(
                        trxId = "TRX11194831",
                        type = "DEPOSIT",
                        method = "bKash",
                        amount = 40000.00,
                        fee = 0.0,
                        recipientOrSender = "bKash: 01712-345678",
                        status = "SUCCESSFUL",
                        timestamp = fourMonthsAgo + 86400_000L * 7,
                        note = "Monthly salary"
                    )
                )
                dao.insertTransaction(
                    TransactionEntity(
                        trxId = "TRX11194832",
                        type = "WITHDRAWAL",
                        method = "Bank Transfer",
                        amount = 16000.00,
                        fee = 240.0,
                        recipientOrSender = "Eastern Bank Ltd",
                        status = "SUCCESSFUL",
                        timestamp = fourMonthsAgo + 86400_000L * 20,
                        note = "Investment deposit"
                    )
                )

                // 5 months ago
                dao.insertTransaction(
                    TransactionEntity(
                        trxId = "TRX00195841",
                        type = "DEPOSIT",
                        method = "bKash",
                        amount = 35000.00,
                        fee = 0.0,
                        recipientOrSender = "bKash: 01712-345678",
                        status = "SUCCESSFUL",
                        timestamp = fiveMonthsAgo + 86400_000L * 10,
                        note = "Freelance web development"
                    )
                )
                dao.insertTransaction(
                    TransactionEntity(
                        trxId = "TRX00195842",
                        type = "WITHDRAWAL",
                        method = "Nagad",
                        amount = 12000.00,
                        fee = 180.0,
                        recipientOrSender = "Nagad: 01911-223344",
                        status = "SUCCESSFUL",
                        timestamp = fiveMonthsAgo + 86400_000L * 22,
                        note = "Monthly living expenses"
                    )
                )

                // 4. Initial Deposit Request
                dao.insertDepositRequest(
                    DepositRequestEntity(
                        trxId = "TRX55192837",
                        method = "Nagad",
                        amount = 5000.00,
                        paymentNumber = "01712-345678",
                        transactionId = "9J8K7L6M",
                        status = "PENDING",
                        timestamp = now - 900_000L,
                        notes = "Agent cash-in at Gulshan Branch"
                    )
                )

                // 5. Initial Notifications
                dao.insertNotification(
                    NotificationEntity(
                        title = "Salary Credited",
                        message = "৳45,000.00 has been credited from City Bank Ltd. Your updated balance is ৳84,500.00.",
                        type = "TRANSACTION",
                        timestamp = twoDaysAgo,
                        isRead = true
                    )
                )
                dao.insertNotification(
                    NotificationEntity(
                        title = "Deposit Pending",
                        message = "Your deposit of ৳5,000.00 via Nagad (TrxID: 9J8K7L6M) is submitted and awaiting admin approval.",
                        type = "DEPOSIT",
                        timestamp = now - 900_000L,
                        isRead = false
                    )
                )
                dao.insertNotification(
                    NotificationEntity(
                        title = "Security Alert",
                        message = "Biometric login was activated successfully on this device.",
                        type = "SECURITY",
                        timestamp = threeDaysAgo,
                        isRead = true
                    )
                )
                dao.insertNotification(
                    NotificationEntity(
                        title = "Cashback Offer 🎉",
                        message = "Get 2% instant cashback on all bKash and Nagad deposits above ৳10,000 this week!",
                        type = "PROMOTION",
                        timestamp = oneDayAgo,
                        isRead = false
                    )
                )

                // 6. Initial Support Ticket
                dao.insertTicket(
                    SupportTicketEntity(
                        subject = "Card Delivery Inquiry",
                        category = "Debit Card",
                        message = "When can I expect my contactless TrustBank debit card to arrive at my mailing address?",
                        status = "RESOLVED",
                        timestamp = twoDaysAgo,
                        reply = "Your debit card has been dispatched via courier (AWB: TB98231) and will arrive within 2 business days."
                    )
                )
            }
        }
    }
}
