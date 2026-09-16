package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransactionEntity
import com.example.ui.BankingViewModel
import com.example.ui.components.MethodLogoBadge
import com.example.ui.components.StatusBadge
import com.example.ui.components.TransactionTrendsCard
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WalletScreen(
    viewModel: BankingViewModel
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val monthlyTrends by viewModel.monthlyTrends.collectAsState()

    val totalDeposits = remember(transactions) {
        transactions.filter { it.type == "DEPOSIT" && it.status == "SUCCESSFUL" }.sumOf { it.amount }
    }
    val totalWithdrawals = remember(transactions) {
        transactions.filter { it.type == "WITHDRAWAL" && it.status == "SUCCESSFUL" }.sumOf { it.amount }
    }

    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredTransactions = remember(transactions, selectedFilter) {
        when (selectedFilter) {
            "DEPOSIT" -> transactions.filter { it.type == "DEPOSIT" }
            "WITHDRAWAL" -> transactions.filter { it.type == "WITHDRAWAL" }
            else -> transactions
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "My Wallet",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Account: ${userProfile.accountNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500
                    )
                }

                FilledTonalButton(
                    onClick = { viewModel.showSnackbar("Wallet Statement exported as PDF") },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = EmeraldContainer)
                ) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = EmeraldDark, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Statement", color = EmeraldDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Available & Pending Balance Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(EmeraldDeep, EmeraldDark)
                            )
                        )
                        .padding(22.dp)
                ) {
                    Column {
                        Text(
                            text = "Available Balance",
                            color = PureWhite.copy(alpha = 0.8f),
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "৳${String.format("%,.2f", userProfile.balance)}",
                            color = PureWhite,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Spacer(modifier = Modifier.height(18.dp))
                        HorizontalDivider(color = PureWhite.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "Pending Balance", color = PureWhite.copy(alpha = 0.7f), fontSize = 11.sp)
                                Text(
                                    text = "৳${String.format("%,.2f", userProfile.pendingBalance)}",
                                    color = SoftGold,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "Currency / Status", color = PureWhite.copy(alpha = 0.7f), fontSize = 11.sp)
                                Text(
                                    text = "BDT (৳) • Active",
                                    color = PureWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Inflow vs Outflow KPI Cards (Total Deposit & Total Withdrawal)
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Deposit Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(text = "Total Deposit", fontSize = 12.sp, color = Slate500, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "৳${String.format("%,.2f", totalDeposits)}",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldDark
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${transactions.count { it.type == "DEPOSIT" }} deposits made",
                            fontSize = 11.sp,
                            color = Slate400
                        )
                    }
                }

                // Withdrawal Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFEE2E2)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = null,
                                    tint = ErrorRed,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(text = "Total Withdraw", fontSize = 12.sp, color = Slate500, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "৳${String.format("%,.2f", totalWithdrawals)}",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${transactions.count { it.type == "WITHDRAWAL" }} cashouts made",
                            fontSize = 11.sp,
                            color = Slate400
                        )
                    }
                }
            }
        }

        // Quick Deposit & Withdraw Buttons
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { viewModel.showAddMoneySheet.value = true },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    modifier = Modifier.weight(1f).height(46.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = PureWhite)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Money", color = PureWhite, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { viewModel.showWithdrawSheet.value = true },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).height(46.dp)
                ) {
                    Icon(imageVector = Icons.Default.Outbox, contentDescription = null, tint = Slate800)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Withdraw", color = Slate800, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Monthly Transaction Trends (Deposits vs Withdrawals)
        item {
            Spacer(modifier = Modifier.height(18.dp))
            TransactionTrendsCard(
                monthlyTrends = monthlyTrends
            )
        }

        // Wallet Statement Section
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Wallet Statement",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                // Statement filter pills
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("ALL", "DEPOSIT", "WITHDRAWAL").forEach { filter ->
                        val isSelected = selectedFilter == filter
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) EmeraldPrimary else Slate100,
                            modifier = Modifier.clickable { selectedFilter = filter }
                        ) {
                            Text(
                                text = if (filter == "ALL") "All" else if (filter == "DEPOSIT") "Deposits" else "Withdraws",
                                color = if (isSelected) PureWhite else Slate600,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        if (filteredTransactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No statement records for this filter.", color = Slate500, fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(filteredTransactions) { trx ->
                StatementTransactionRow(
                    transaction = trx,
                    onClick = { viewModel.selectedTransaction.value = trx }
                )
            }
        }
    }
}

@Composable
fun StatementTransactionRow(
    transaction: TransactionEntity,
    onClick: () -> Unit
) {
    val formattedDate = remember(transaction.timestamp) {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        sdf.format(Date(transaction.timestamp))
    }
    val isPositive = transaction.type == "DEPOSIT" || transaction.type == "RECEIVE"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MethodLogoBadge(transaction.method)
                Column {
                    Text(
                        text = "${transaction.type}: ${transaction.recipientOrSender}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Slate900
                    )
                    Text(
                        text = "$formattedDate • ${transaction.trxId}",
                        fontSize = 11.sp,
                        color = Slate500
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isPositive) "+" else "-"}৳${String.format("%,.2f", transaction.amount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (isPositive) EmeraldDark else Slate900
                )
                StatusBadge(transaction.status)
            }
        }
    }
}
