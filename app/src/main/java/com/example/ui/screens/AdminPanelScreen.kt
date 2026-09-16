package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.BankingViewModel
import com.example.ui.components.MethodLogoBadge
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun AdminPanelScreen(
    viewModel: BankingViewModel
) {
    val adminConfig by viewModel.adminConfig.collectAsState()
    val depositRequests by viewModel.depositRequests.collectAsState()
    val withdrawalRequests by viewModel.withdrawalRequests.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val transactions by viewModel.transactions.collectAsState()

    var selectedAdminTab by remember { mutableStateOf("DEPOSITS") }

    val pendingDeposits = remember(depositRequests) { depositRequests.filter { it.status == "PENDING" } }
    val pendingWithdrawals = remember(withdrawalRequests) { withdrawalRequests.filter { it.status == "PENDING" } }

    val totalDepositsSum = remember(transactions) {
        transactions.filter { it.type == "DEPOSIT" && it.status == "SUCCESSFUL" }.sumOf { it.amount }
    }
    val totalWithdrawalsSum = remember(transactions) {
        transactions.filter { it.type == "WITHDRAWAL" && it.status == "SUCCESSFUL" }.sumOf { it.amount }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // 1. Top Admin Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate900)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(EmeraldPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = null, tint = PureWhite, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text(text = "Admin Control Center", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PureWhite)
                        Text(text = "Root Privilege Session", fontSize = 11.sp, color = Slate400)
                    }
                }

                Button(
                    onClick = { viewModel.toggleAdminMode(false) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(imageVector = Icons.Default.ExitToApp, contentDescription = null, tint = PureWhite, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Exit Admin", color = PureWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 2. System Analytics & KPI Grid
        item {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "System Analytics",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AdminMetricCard(
                        modifier = Modifier.weight(1f),
                        title = "Pending Queue",
                        value = "${pendingDeposits.size + pendingWithdrawals.size}",
                        accentColor = AmberGold
                    )
                    AdminMetricCard(
                        modifier = Modifier.weight(1f),
                        title = "User Balance",
                        value = "৳${String.format("%,.0f", userProfile.balance)}",
                        accentColor = EmeraldPrimary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AdminMetricCard(
                        modifier = Modifier.weight(1f),
                        title = "Total Volume (In)",
                        value = "৳${String.format("%,.0f", totalDepositsSum)}",
                        accentColor = InfoBlue
                    )
                    AdminMetricCard(
                        modifier = Modifier.weight(1f),
                        title = "Total Volume (Out)",
                        value = "৳${String.format("%,.0f", totalWithdrawalsSum)}",
                        accentColor = ErrorRed
                    )
                }
            }
        }

        // 3. Admin Navigation Tabs
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val tabs = listOf(
                    "DEPOSITS" to "Deposits (${pendingDeposits.size})",
                    "WITHDRAWALS" to "Withdrawals (${pendingWithdrawals.size})",
                    "KYC" to "KYC Review",
                    "BALANCE" to "Balance Manager",
                    "GATEWAYS" to "bKash / Nagad",
                    "FEES" to "Fees & Limits",
                    "BROADCAST" to "Broadcast"
                )
                items(tabs) { (key, label) ->
                    val isSel = selectedAdminTab == key
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSel) EmeraldPrimary else Slate100,
                        modifier = Modifier.clickable { selectedAdminTab = key }
                    ) {
                        Text(
                            text = label,
                            color = if (isSel) PureWhite else Slate800,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // 4. Tab Contents
        when (selectedAdminTab) {
            "DEPOSITS" -> {
                item {
                    Text(
                        text = "Deposit Approval Queue (${depositRequests.size} total)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (depositRequests.isEmpty()) {
                    item {
                        EmptyStateCard("No deposit requests on record.")
                    }
                } else {
                    items(depositRequests) { req ->
                        AdminDepositCard(
                            request = req,
                            onApprove = { viewModel.approveDeposit(req.id) },
                            onReject = { viewModel.rejectDeposit(req.id) }
                        )
                    }
                }
            }

            "WITHDRAWALS" -> {
                item {
                    Text(
                        text = "Cashout Approval Queue (${withdrawalRequests.size} total)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (withdrawalRequests.isEmpty()) {
                    item {
                        EmptyStateCard("No withdrawal requests on record.")
                    }
                } else {
                    items(withdrawalRequests) { req ->
                        AdminWithdrawalCard(
                            request = req,
                            onApprove = { viewModel.approveWithdrawal(req.id) },
                            onReject = { viewModel.rejectWithdrawal(req.id) }
                        )
                    }
                }
            }

            "KYC" -> {
                item {
                    AdminKycPanel(viewModel = viewModel)
                }
            }

            "BALANCE" -> {
                item {
                    AdminBalancePanel(viewModel = viewModel)
                }
            }

            "GATEWAYS" -> {
                item {
                    AdminGatewayPanel(viewModel = viewModel)
                }
            }

            "FEES" -> {
                item {
                    AdminFeesPanel(viewModel = viewModel)
                }
            }

            "BROADCAST" -> {
                item {
                    AdminBroadcastPanel(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun AdminMetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    accentColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = title, fontSize = 11.sp, color = Slate500, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = accentColor)
        }
    }
}

@Composable
fun AdminDepositCard(
    request: com.example.data.model.DepositRequestEntity,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 5.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MethodLogoBadge(request.method)
                    Column {
                        Text(text = "${request.method} Deposit", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = "From: ${request.paymentNumber}", fontSize = 11.sp, color = Slate500)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "+৳${String.format("%,.2f", request.amount)}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = EmeraldDark)
                    StatusBadge(request.status)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "TrxID: ${request.transactionId}", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Slate700)

            if (request.status == "PENDING") {
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier.weight(1f).height(38.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed)
                    ) {
                        Text("Reject", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onApprove,
                        modifier = Modifier.weight(1f).height(38.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Text("Approve & Credit", fontSize = 12.sp, color = PureWhite, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminWithdrawalCard(
    request: com.example.data.model.WithdrawalRequestEntity,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 5.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MethodLogoBadge(request.method)
                    Column {
                        Text(text = "${request.method} Cashout", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = "To: ${request.targetAccount}", fontSize = 11.sp, color = Slate500)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "-৳${String.format("%,.2f", request.amount)}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Slate900)
                    StatusBadge(request.status)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Fee: ৳${String.format("%,.2f", request.fee)}", fontSize = 11.sp, color = Slate500)

            if (request.status == "PENDING") {
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier.weight(1f).height(38.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed)
                    ) {
                        Text("Reject & Refund", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onApprove,
                        modifier = Modifier.weight(1f).height(38.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Text("Approve Cashout", fontSize = 12.sp, color = PureWhite, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminKycPanel(viewModel: BankingViewModel) {
    val userProfile by viewModel.userProfile.collectAsState()

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "KYC Verification Management", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(12.dp))

            Text("Applicant: ${userProfile.name}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text("Legal Name: ${userProfile.kycLegalName.ifBlank { "Not provided" }}", fontSize = 12.sp, color = Slate600)
            Text("Document Type: ${userProfile.kycDocumentType}", fontSize = 12.sp, color = Slate600)
            Text("Doc Number: ${userProfile.kycDocNumber.ifBlank { "Not provided" }}", fontSize = 12.sp, color = Slate600)
            Text("Date of Birth: ${userProfile.kycDateOfBirth.ifBlank { "Not provided" }}", fontSize = 12.sp, color = Slate600)

            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Verification Status:", fontSize = 12.sp, color = Slate700)
                StatusBadge(userProfile.kycStatus)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = { viewModel.updateKycStatus("REJECTED") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Reject KYC", color = ErrorRed)
                }
                Button(
                    onClick = { viewModel.updateKycStatus("VERIFIED") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Approve KYC", color = PureWhite)
                }
            }
        }
    }
}

@Composable
fun AdminBalancePanel(viewModel: BankingViewModel) {
    val userProfile by viewModel.userProfile.collectAsState()
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Manual Balance Adjustment", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(text = "Target: ${userProfile.name} (${userProfile.accountNumber})", fontSize = 12.sp, color = Slate500)
            Text(text = "Current Balance: ৳${String.format("%,.2f", userProfile.balance)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EmeraldDark)

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Adjustment Amount (৳)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Reason / Reference Note") },
                placeholder = { Text("e.g. Compensation, dispute refund, test bonus") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = {
                        val amt = amount.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            viewModel.adjustUserBalance(amt, isCredit = true, note = note)
                            amount = ""
                            note = ""
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("+ Credit (Add)", color = PureWhite, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        val amt = amount.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            viewModel.adjustUserBalance(amt, isCredit = false, note = note)
                            amount = ""
                            note = ""
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("- Debit (Deduct)", color = PureWhite, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AdminGatewayPanel(viewModel: BankingViewModel) {
    val config by viewModel.adminConfig.collectAsState()
    var bkash by remember { mutableStateOf(config.bkashNumber) }
    var nagad by remember { mutableStateOf(config.nagadNumber) }
    var bankAccount by remember { mutableStateOf(config.bankAccountNumber) }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Payment Gateway Numbers", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(text = "Configure receiving numbers displayed to users in Add Money", fontSize = 11.sp, color = Slate500)

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = bkash,
                onValueChange = { bkash = it },
                label = { Text("bKash Merchant / Agent Number") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = nagad,
                onValueChange = { nagad = it },
                label = { Text("Nagad Merchant / Agent Number") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = bankAccount,
                onValueChange = { bankAccount = it },
                label = { Text("Bank Account Number") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.updateGateways(bkash, nagad, bankAccount)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save Gateway Numbers", color = PureWhite, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AdminFeesPanel(viewModel: BankingViewModel) {
    val config by viewModel.adminConfig.collectAsState()
    var fee by remember { mutableStateOf(config.withdrawalFeePercent.toString()) }
    var minDep by remember { mutableStateOf(config.minDeposit.toString()) }
    var maxDep by remember { mutableStateOf(config.maxDeposit.toString()) }
    var minWith by remember { mutableStateOf(config.minWithdrawal.toString()) }
    var maxWith by remember { mutableStateOf(config.maxWithdrawal.toString()) }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Fee & Limit Configuration", fontWeight = FontWeight.Bold, fontSize = 15.sp)

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = fee,
                onValueChange = { fee = it },
                label = { Text("Withdrawal Fee Percentage (%)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = minDep,
                    onValueChange = { minDep = it },
                    label = { Text("Min Deposit (৳)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    value = maxDep,
                    onValueChange = { maxDep = it },
                    label = { Text("Max Deposit (৳)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = minWith,
                    onValueChange = { minWith = it },
                    label = { Text("Min Cashout (৳)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    value = maxWith,
                    onValueChange = { maxWith = it },
                    label = { Text("Max Cashout (৳)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val f = fee.toDoubleOrNull() ?: 1.5
                    val miD = minDep.toDoubleOrNull() ?: 100.0
                    val maD = maxDep.toDoubleOrNull() ?: 100000.0
                    val miW = minWith.toDoubleOrNull() ?: 100.0
                    val maW = maxWith.toDoubleOrNull() ?: 50000.0
                    viewModel.updateFeesAndLimits(f, miD, maD, miW, maW)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save Fees & Limits", color = PureWhite, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AdminBroadcastPanel(viewModel: BankingViewModel) {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("PROMOTION") }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Send Global Notification", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(text = "Broadcast marketing or emergency notices to all active users", fontSize = 11.sp, color = Slate500)

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Notification Title") },
                placeholder = { Text("e.g. 10% Cashback on Nagad Deposits!") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("Notification Message") },
                placeholder = { Text("Full promotional or security body text...") },
                modifier = Modifier.fillMaxWidth().height(90.dp),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (title.isNotBlank() && message.isNotBlank()) {
                        viewModel.sendBroadcastNotification(title, message, type)
                        title = ""
                        message = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Campaign, contentDescription = null, tint = PureWhite)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Broadcast to Users", color = PureWhite, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun EmptyStateCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
            Text(text = message, color = Slate500, fontSize = 13.sp)
        }
    }
}
