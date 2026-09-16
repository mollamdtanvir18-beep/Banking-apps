package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransactionEntity
import com.example.ui.BankingViewModel
import com.example.ui.components.MethodLogoBadge
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TransactionsScreen(
    viewModel: BankingViewModel
) {
    val transactions by viewModel.transactions.collectAsState()
    val typeFilter by viewModel.transactionTypeFilter.collectAsState()
    val statusFilter by viewModel.transactionStatusFilter.collectAsState()
    val searchQuery by viewModel.transactionSearchQuery.collectAsState()

    val filteredTransactions = remember(transactions, typeFilter, statusFilter, searchQuery) {
        transactions.filter { trx ->
            val matchesType = when (typeFilter) {
                "ALL" -> true
                "DEPOSIT" -> trx.type == "DEPOSIT"
                "WITHDRAWAL" -> trx.type == "WITHDRAWAL"
                "SEND" -> trx.type == "SEND"
                "RECEIVE" -> trx.type == "RECEIVE"
                else -> true
            }

            val matchesStatus = when (statusFilter) {
                "ALL" -> true
                "SUCCESSFUL" -> trx.status == "SUCCESSFUL"
                "PENDING" -> trx.status == "PENDING"
                "FAILED" -> trx.status == "FAILED"
                else -> true
            }

            val matchesSearch = if (searchQuery.isBlank()) true else {
                trx.trxId.contains(searchQuery, ignoreCase = true) ||
                trx.recipientOrSender.contains(searchQuery, ignoreCase = true) ||
                trx.method.contains(searchQuery, ignoreCase = true) ||
                trx.note.contains(searchQuery, ignoreCase = true)
            }

            matchesType && matchesStatus && matchesSearch
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Transactions Ledger",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${filteredTransactions.size} transactions found",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setTransactionFilters(query = it) },
                    placeholder = { Text("Search by TrxID, sender, note...") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = Slate400) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setTransactionFilters(query = "") }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = Slate500)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        }

        // Type Filter Chips (All, Deposit, Withdrawal, Send Money, Receive Money)
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val types = listOf(
                    "ALL" to "All",
                    "DEPOSIT" to "Deposit",
                    "WITHDRAWAL" to "Withdrawal",
                    "SEND" to "Send Money",
                    "RECEIVE" to "Receive Money"
                )
                items(types) { (key, label) ->
                    val isSelected = typeFilter == key
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setTransactionFilters(type = key) },
                        label = { Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldPrimary,
                            selectedLabelColor = PureWhite,
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Status Filter Chips (All, Successful, Pending, Failed)
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val statuses = listOf(
                    "ALL" to "All Statuses",
                    "SUCCESSFUL" to "Successful",
                    "PENDING" to "Pending",
                    "FAILED" to "Failed"
                )
                items(statuses) { (key, label) ->
                    val isSelected = statusFilter == key
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setTransactionFilters(status = key) },
                        label = { Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Slate800,
                            selectedLabelColor = PureWhite,
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Transactions List
        if (filteredTransactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = null, tint = Slate300, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "No matching transactions", fontWeight = FontWeight.Bold, color = Slate700)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Try adjusting your search or filter tags.", color = Slate500, fontSize = 12.sp)
                    }
                }
            }
        } else {
            items(filteredTransactions) { trx ->
                DetailedTransactionCard(
                    transaction = trx,
                    onClick = { viewModel.selectedTransaction.value = trx }
                )
            }
        }
    }
}

@Composable
fun DetailedTransactionCard(
    transaction: TransactionEntity,
    onClick: () -> Unit
) {
    val formattedDate = remember(transaction.timestamp) {
        val sdf = SimpleDateFormat("dd MMM yyyy • hh:mm a", Locale.getDefault())
        sdf.format(Date(transaction.timestamp))
    }
    val isPositive = transaction.type == "DEPOSIT" || transaction.type == "RECEIVE"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 5.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    MethodLogoBadge(transaction.method)
                    Column {
                        Text(
                            text = transaction.recipientOrSender,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${transaction.type} via ${transaction.method}",
                            fontSize = 11.sp,
                            color = Slate500
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${if (isPositive) "+" else "-"}৳${String.format("%,.2f", transaction.amount)}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = if (isPositive) EmeraldDark else Slate900
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    StatusBadge(transaction.status)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Slate100)
            Spacer(modifier = Modifier.height(8.dp))

            // Footer row with TrxID and Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ID: ${transaction.trxId}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = EmeraldDark
                )
                Text(
                    text = formattedDate,
                    fontSize = 11.sp,
                    color = Slate400
                )
            }
        }
    }
}
