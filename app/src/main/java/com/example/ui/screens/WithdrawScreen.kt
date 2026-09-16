package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WithdrawalRequestEntity
import com.example.ui.BankingViewModel
import com.example.ui.components.MethodLogoBadge
import com.example.ui.components.PinVerificationDialog
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithdrawSheet(
    viewModel: BankingViewModel,
    onDismiss: () -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val adminConfig by viewModel.adminConfig.collectAsState()
    val withdrawalRequests by viewModel.withdrawalRequests.collectAsState()

    var selectedMethod by remember { mutableStateOf("bKash") }
    var amountText by remember { mutableStateOf("") }
    var targetAccount by remember { mutableStateOf("") }
    var userNotes by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showPinDialog by remember { mutableStateOf(false) }

    val presetAmounts = listOf("500", "1000", "3000", "5000", "10000", "20000")

    val enteredAmount = amountText.toDoubleOrNull() ?: 0.0
    val feePercent = adminConfig.withdrawalFeePercent
    val calculatedFee = (enteredAmount * feePercent / 100.0)
    val totalDeduction = enteredAmount + calculatedFee

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Withdraw Money",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Cash out funds to your mobile wallet or bank account",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Available Balance Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = EmeraldContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Available to Withdraw", color = Slate600, fontSize = 12.sp)
                            Text(
                                text = "৳${String.format("%,.2f", userProfile.balance)}",
                                color = EmeraldDark,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = EmeraldPrimary
                        ) {
                            Text(
                                text = "Fee: $feePercent%",
                                color = PureWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Method Selector: bKash, Nagad, Bank Account
            item {
                Text(
                    text = "Select Cashout Destination",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf("bKash", "Nagad", "Bank Account").forEach { method ->
                        val isSelected = selectedMethod == method
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    selectedMethod = method
                                    errorMessage = null
                                },
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) EmeraldContainer else Slate50,
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, EmeraldPrimary) else null
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                MethodLogoBadge(method)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = method,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) EmeraldDark else Slate700
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Enter Amount
            item {
                Text(
                    text = "Withdrawal Amount (৳)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    placeholder = { Text("e.g. 5000") },
                    leadingIcon = { Text("৳", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = EmeraldPrimary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presetAmounts.forEach { preset ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (amountText == preset) EmeraldPrimary else Slate100,
                            modifier = Modifier.clickable { amountText = preset }
                        ) {
                            Text(
                                text = "৳$preset",
                                color = if (amountText == preset) PureWhite else Slate700,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Account / Wallet Number
            item {
                Text(
                    text = if (selectedMethod == "Bank Account") "Destination Bank Account Number" else "Recipient $selectedMethod Mobile Number",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = targetAccount,
                    onValueChange = { targetAccount = it },
                    placeholder = { Text(if (selectedMethod == "Bank Account") "e.g. 102948271039" else "e.g. 017XXXXXXXX") },
                    leadingIcon = { Icon(imageVector = Icons.Default.AccountBalanceWallet, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Fee & Calculation Card
            if (enteredAmount > 0.0) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate50),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Withdrawal Amount", color = Slate600, fontSize = 13.sp)
                                Text("৳${String.format("%,.2f", enteredAmount)}", fontWeight = FontWeight.SemiBold, color = Slate900, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Processing Fee ($feePercent%)", color = Slate600, fontSize = 13.sp)
                                Text("৳${String.format("%,.2f", calculatedFee)}", fontWeight = FontWeight.SemiBold, color = ErrorRed, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            HorizontalDivider(color = Slate200)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Deduction", fontWeight = FontWeight.Bold, color = Slate900, fontSize = 14.sp)
                                Text("৳${String.format("%,.2f", totalDeduction)}", fontWeight = FontWeight.Bold, color = Slate900, fontSize = 14.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }

            // Error display
            errorMessage?.let {
                item {
                    Text(
                        text = it,
                        color = ErrorRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            // Confirm Withdrawal Button
            item {
                Button(
                    onClick = {
                        val amt = amountText.toDoubleOrNull()
                        if (amt == null || amt < adminConfig.minWithdrawal) {
                            errorMessage = "Minimum withdrawal amount is ৳${adminConfig.minWithdrawal}"
                            return@Button
                        }
                        if (amt > adminConfig.maxWithdrawal) {
                            errorMessage = "Maximum withdrawal limit is ৳${adminConfig.maxWithdrawal}"
                            return@Button
                        }
                        if (targetAccount.isBlank()) {
                            errorMessage = "Please enter your destination wallet or account number"
                            return@Button
                        }
                        if (totalDeduction > userProfile.balance) {
                            errorMessage = "Insufficient balance. Total required: ৳${String.format("%,.2f", totalDeduction)}"
                            return@Button
                        }

                        errorMessage = null
                        showPinDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = PureWhite)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Confirm Withdrawal",
                        color = PureWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(28.dp))
            }

            // Withdrawal Status Section
            item {
                Text(
                    text = "Withdrawal History & Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (withdrawalRequests.isEmpty()) {
                item {
                    Text(
                        text = "No withdrawal requests yet.",
                        color = Slate500,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            } else {
                items(withdrawalRequests) { req ->
                    WithdrawalRequestRow(request = req)
                }
            }
        }
    }

    if (showPinDialog) {
        PinVerificationDialog(
            title = "Authorize Cashout",
            subtitle = "Enter your 4-digit PIN to withdraw ৳${String.format("%,.2f", enteredAmount)} to $targetAccount",
            onDismiss = { showPinDialog = false },
            onPinEntered = { enteredPin ->
                showPinDialog = false
                viewModel.submitWithdrawal(
                    method = selectedMethod,
                    amount = enteredAmount,
                    targetAccount = targetAccount,
                    pin = enteredPin,
                    notes = userNotes
                ) { success, msg ->
                    if (!success) {
                        errorMessage = msg
                    }
                }
            }
        )
    }
}

@Composable
fun WithdrawalRequestRow(request: WithdrawalRequestEntity) {
    val dateStr = remember(request.timestamp) {
        val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        sdf.format(Date(request.timestamp))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Slate50),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MethodLogoBadge(request.method)
                Column {
                    Text(
                        text = "${request.method} Cashout",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Slate800
                    )
                    Text(
                        text = "To: ${request.targetAccount}",
                        fontSize = 11.sp,
                        color = Slate500
                    )
                    Text(
                        text = dateStr,
                        fontSize = 10.sp,
                        color = Slate400
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "-৳${String.format("%,.2f", request.amount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Slate900
                )
                Text(
                    text = "Fee: ৳${String.format("%,.2f", request.fee)}",
                    fontSize = 10.sp,
                    color = Slate500
                )
                Spacer(modifier = Modifier.height(2.dp))
                StatusBadge(request.status)
            }
        }
    }
}
