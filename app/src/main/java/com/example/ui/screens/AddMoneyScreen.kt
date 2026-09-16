package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DepositRequestEntity
import com.example.ui.BankingViewModel
import com.example.ui.components.MethodLogoBadge
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMoneySheet(
    viewModel: BankingViewModel,
    onDismiss: () -> Unit
) {
    val adminConfig by viewModel.adminConfig.collectAsState()
    val depositRequests by viewModel.depositRequests.collectAsState()
    val clipboardManager = LocalClipboardManager.current

    var selectedMethod by remember { mutableStateOf("bKash") }
    var amountText by remember { mutableStateOf("") }
    var paymentNumber by remember { mutableStateOf("") }
    var transactionId by remember { mutableStateOf("") }
    var userNotes by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var copiedMerchant by remember { mutableStateOf(false) }

    val presetAmounts = listOf("500", "1000", "2000", "5000", "10000", "25000")

    val merchantNumber = when (selectedMethod) {
        "bKash" -> adminConfig.bkashNumber
        "Nagad" -> adminConfig.nagadNumber
        else -> adminConfig.bankAccountNumber
    }

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
                            text = "Add Money",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Deposit funds safely into your TrustBank account",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Method Selector: bKash, Nagad, Bank Transfer
            item {
                Text(
                    text = "Select Payment Method",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf("bKash", "Nagad", "Bank Transfer").forEach { method ->
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

            // Payment Instructions Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate50),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (selectedMethod == "Bank Transfer") "Bank Transfer Details" else "Send Money to Merchant",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Slate800
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = EmeraldContainer
                            ) {
                                Text(
                                    text = "0% Fee (Free)",
                                    color = EmeraldDark,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (selectedMethod == "Bank Transfer") {
                            Text(text = "Bank: ${adminConfig.bankName}", fontSize = 12.sp, color = Slate700)
                            Text(text = "Account Name: ${adminConfig.bankAccountName}", fontSize = 12.sp, color = Slate700)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = "Account No: ${adminConfig.bankAccountNumber}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate900)
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(adminConfig.bankAccountNumber))
                                        copiedMerchant = true
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = EmeraldPrimary, modifier = Modifier.size(15.dp))
                                }
                            }
                            Text(text = "Routing No: ${adminConfig.bankRouting}", fontSize = 12.sp, color = Slate700)
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = merchantNumber,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldDark
                                )
                                Button(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(merchantNumber.substringBefore(" ")))
                                        copiedMerchant = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("Copy Number", fontSize = 11.sp, color = PureWhite)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "1. Open your $selectedMethod app and Send Money to the merchant number.\n2. Note down the Transaction ID (TrxID) and fill the form below.",
                                fontSize = 11.sp,
                                color = Slate600,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Amount Input
            item {
                Text(
                    text = "Deposit Amount (৳)",
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

                // Amount preset chips
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

            // Payment Number
            item {
                Text(
                    text = if (selectedMethod == "Bank Transfer") "Your Bank Account Number / Holder" else "Your $selectedMethod Account / Mobile Number",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = paymentNumber,
                    onValueChange = { paymentNumber = it },
                    placeholder = { Text(if (selectedMethod == "Bank Transfer") "e.g. 205018291039" else "e.g. 01712-XXXXXX") },
                    leadingIcon = { Icon(imageVector = Icons.Default.PhoneAndroid, contentDescription = "Phone") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Transaction ID
            item {
                Text(
                    text = "Transaction ID (TrxID)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = transactionId,
                    onValueChange = { transactionId = it },
                    placeholder = { Text("e.g. 9J8K7L6M") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Tag, contentDescription = "TrxID") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Optional note
            item {
                Text(
                    text = "Deposit Note (Optional)",
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = Slate600
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = userNotes,
                    onValueChange = { userNotes = it },
                    placeholder = { Text("e.g. Branch deposit or personal savings") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
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

            // Submit Button
            item {
                Button(
                    onClick = {
                        val amt = amountText.toDoubleOrNull()
                        if (amt == null || amt < adminConfig.minDeposit) {
                            errorMessage = "Minimum deposit amount is ৳${adminConfig.minDeposit}"
                            return@Button
                        }
                        if (amt > adminConfig.maxDeposit) {
                            errorMessage = "Maximum deposit limit is ৳${adminConfig.maxDeposit}"
                            return@Button
                        }
                        if (paymentNumber.isBlank()) {
                            errorMessage = "Please enter your payment phone/account number"
                            return@Button
                        }
                        if (transactionId.isBlank()) {
                            errorMessage = "Please enter the Transaction ID (TrxID)"
                            return@Button
                        }

                        errorMessage = null
                        viewModel.submitDeposit(
                            method = selectedMethod,
                            amount = amt,
                            senderNumber = paymentNumber,
                            trxId = transactionId,
                            notes = userNotes
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = PureWhite)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Submit Deposit Request",
                        color = PureWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(28.dp))
            }

            // Deposit Status Section
            item {
                Text(
                    text = "Recent Deposit Requests",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (depositRequests.isEmpty()) {
                item {
                    Text(
                        text = "No deposit requests yet.",
                        color = Slate500,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            } else {
                items(depositRequests) { req ->
                    DepositRequestRow(request = req)
                }
            }
        }
    }
}

@Composable
fun DepositRequestRow(request: DepositRequestEntity) {
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
                        text = "${request.method} Deposit",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Slate800
                    )
                    Text(
                        text = "TrxID: ${request.transactionId}",
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
                    text = "+৳${String.format("%,.2f", request.amount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = EmeraldDark
                )
                Spacer(modifier = Modifier.height(3.dp))
                StatusBadge(request.status)
            }
        }
    }
}
