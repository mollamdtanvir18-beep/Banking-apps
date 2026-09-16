package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.NotificationEntity
import com.example.ui.BankingViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsSheet(
    viewModel: BankingViewModel,
    onDismiss: () -> Unit
) {
    val notifications by viewModel.notifications.collectAsState()
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredNotifications = remember(notifications, selectedFilter) {
        if (selectedFilter == "ALL") notifications
        else notifications.filter { it.type == selectedFilter }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Notifications",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Live transaction alerts, deposit updates & security notices",
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

            // Quick actions (Mark all read / Clear)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { viewModel.markAllNotificationsAsRead() }) {
                        Text("Mark all read", color = EmeraldPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    TextButton(onClick = { viewModel.clearAllNotifications() }) {
                        Text("Clear history", color = ErrorRed, fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Notification Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val filterChips = listOf(
                        "ALL" to "All",
                        "DEPOSIT" to "Deposits",
                        "WITHDRAWAL" to "Withdrawals",
                        "TRANSACTION" to "Transactions",
                        "PROMOTION" to "Promotions",
                        "SECURITY" to "Security"
                    )
                    items(filterChips) { (key, label) ->
                        val isSel = selectedFilter == key
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSel) EmeraldPrimary else Slate100,
                            modifier = Modifier.clickable { selectedFilter = key }
                        ) {
                            Text(
                                text = label,
                                color = if (isSel) PureWhite else Slate700,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            if (filteredNotifications.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "No notifications in this category", color = Slate500, fontSize = 13.sp)
                    }
                }
            } else {
                items(filteredNotifications) { item ->
                    NotificationRow(item = item, onClick = { viewModel.markNotificationAsRead(item.id) })
                }
            }
        }
    }
}

@Composable
fun NotificationRow(
    item: NotificationEntity,
    onClick: () -> Unit
) {
    val dateStr = remember(item.timestamp) {
        val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        sdf.format(Date(item.timestamp))
    }

    val (icon, bg, tint) = when (item.type) {
        "DEPOSIT" -> Triple(Icons.Default.ArrowDownward, EmeraldContainer, EmeraldDark)
        "WITHDRAWAL" -> Triple(Icons.Default.ArrowUpward, Color(0xFFFEE2E2), ErrorRed)
        "PROMOTION" -> Triple(Icons.Default.Celebration, SoftGold, AmberGold)
        "SECURITY" -> Triple(Icons.Default.Shield, Color(0xFFEFF6FF), InfoBlue)
        else -> Triple(Icons.Default.ReceiptLong, EmeraldContainer, EmeraldDark)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = if (item.isRead) Slate50 else PureWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (item.isRead) Slate100 else EmeraldPrimary.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(bg),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.title,
                        fontWeight = if (item.isRead) FontWeight.SemiBold else FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = dateStr,
                        fontSize = 10.sp,
                        color = Slate400
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = item.message,
                    fontSize = 12.sp,
                    color = Slate600,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun SupportScreen(
    viewModel: BankingViewModel
) {
    val tickets by viewModel.supportTickets.collectAsState()

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
                    text = "Help & Support",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "We are here for you 24/7 across multiple channels",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500
                )
            }
        }

        // Live Chat Hero Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clickable { viewModel.showLiveChatDialog.value = true },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldContainer)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(EmeraldPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Chat, contentDescription = null, tint = PureWhite, modifier = Modifier.size(26.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Live Chat Assistant",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = EmeraldDark
                        )
                        Text(
                            text = "Instant automated AI banking replies for deposits, transfers, and account status.",
                            fontSize = 12.sp,
                            color = Slate700
                        )
                    }
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = EmeraldDark)
                }
            }
        }

        // Contact Channels (Hotline, Email, WhatsApp)
        item {
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Contact Support",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ContactCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Phone,
                    title = "Hotline",
                    subtitle = "16299 (24/7)",
                    onClick = { viewModel.showSnackbar("Dialing TrustBank 24/7 Hotline 16299...") }
                )
                ContactCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Email,
                    title = "Email",
                    subtitle = "support@bank.com",
                    onClick = { viewModel.showSnackbar("Opening email to support@trustbank.com") }
                )
                ContactCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Message,
                    title = "WhatsApp",
                    subtitle = "+880 1700-000",
                    onClick = { viewModel.showSnackbar("Opening TrustBank WhatsApp Desk...") }
                )
            }
        }

        // Report a Problem / Create Ticket
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clickable { viewModel.showReportProblemDialog.value = true },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ReportProblem, contentDescription = null, tint = WarningOrange)
                        Column {
                            Text(text = "Report a Problem", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text(text = "Submit a formal banking inquiry or ticket", fontSize = 11.sp, color = Slate500)
                        }
                    }
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Slate400)
                }
            }
        }

        // Frequently Asked Questions
        item {
            Spacer(modifier = Modifier.height(22.dp))
            Text(
                text = "Frequently Asked Questions",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        val faqs = listOf(
            "How long does bKash / Nagad deposit take?" to "bKash and Nagad deposits are approved within 2-5 minutes of submission by our automated clearing queue.",
            "What are the withdrawal fees?" to "Mobile wallet cashout fee is 1.5%. Bank transfer withdrawals have a flat fee of ৳10.",
            "Why is my KYC verification required?" to "KYC (Know Your Customer) is mandated by Bangladesh Bank to prevent fraudulent activities and unlock daily limits over ৳25,000.",
            "How do I reset my transaction PIN?" to "You can update your 4-digit PIN in the Profile tab under 'Change Transaction PIN'. The initial default PIN is 1234."
        )

        items(faqs) { (question, answer) ->
            FaqItem(question = question, answer = answer)
        }
    }
}

@Composable
fun ContactCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape).background(EmeraldContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = EmeraldDark, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(text = subtitle, fontSize = 10.sp, color = Slate500)
        }
    }
}

@Composable
fun FaqItem(question: String, answer: String) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = question,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = Slate800,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = Slate500
                )
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = answer,
                    fontSize = 12.sp,
                    color = Slate600,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

// Live Chat Dialog
@Composable
fun LiveChatDialog(
    viewModel: BankingViewModel,
    onDismiss: () -> Unit
) {
    val messages by viewModel.chatMessages.collectAsState()
    var input by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().height(520.dp).padding(4.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(EmeraldPrimary)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(imageVector = Icons.Default.SupportAgent, contentDescription = null, tint = PureWhite)
                        Column {
                            Text("TrustBank AI Assistant", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Online • Instant Responses", color = PureWhite.copy(alpha = 0.8f), fontSize = 10.sp)
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = PureWhite)
                    }
                }

                // Chat Messages List
                LazyColumn(
                    modifier = Modifier.weight(1f).padding(horizontal = 12.dp, vertical = 8.dp),
                    reverseLayout = false
                ) {
                    items(messages) { msg ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start
                        ) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (msg.isUser) EmeraldPrimary else Slate100,
                                modifier = Modifier.widthIn(max = 260.dp)
                            ) {
                                Text(
                                    text = msg.text,
                                    color = if (msg.isUser) PureWhite else Slate900,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(10.dp),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }

                // Quick suggestions
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("Balance", "Deposit info", "Withdraw fee").forEach { chip ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = EmeraldContainer,
                            modifier = Modifier.clickable {
                                viewModel.sendChatMessage(chip)
                            }
                        ) {
                            Text(text = chip, fontSize = 11.sp, color = EmeraldDark, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                        }
                    }
                }

                // Input Row
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        placeholder = { Text("Ask about balance, transfer...", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = {
                            if (input.isNotBlank()) {
                                val text = input
                                input = ""
                                viewModel.sendChatMessage(text)
                            }
                        },
                        modifier = Modifier.size(42.dp).background(EmeraldPrimary, CircleShape)
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = PureWhite, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

// Report Problem Dialog
@Composable
fun ReportProblemDialog(
    viewModel: BankingViewModel,
    onDismiss: () -> Unit
) {
    var subject by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Deposit") }
    var description by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(6.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = "Submit Support Ticket", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(14.dp))

                Text(text = "Category", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Deposit", "Withdraw", "KYC", "Other").forEach { cat ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (category == cat) EmeraldPrimary else Slate100,
                            modifier = Modifier.weight(1f).clickable { category = cat }
                        ) {
                            Box(modifier = Modifier.padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                                Text(cat, color = if (category == cat) PureWhite else Slate800, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject") },
                    placeholder = { Text("e.g. Deposit confirmation delay") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    placeholder = { Text("Provide details of your transaction or inquiry...") },
                    modifier = Modifier.fillMaxWidth().height(110.dp),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = Slate600) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (subject.isNotBlank()) {
                                viewModel.submitSupportTicket(subject, category, description)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Submit Ticket", color = PureWhite)
                    }
                }
            }
        }
    }
}
