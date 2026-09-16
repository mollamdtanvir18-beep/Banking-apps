package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransactionEntity
import com.example.ui.BankingViewModel
import com.example.ui.NavigationTab
import com.example.ui.components.MethodLogoBadge
import com.example.ui.components.StatusBadge
import com.example.ui.components.TransactionTrendsCard
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    viewModel: BankingViewModel,
    onNavigateToTransactions: () -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val monthlyTrends by viewModel.monthlyTrends.collectAsState()
    val unreadNotifications = remember(notifications) { notifications.count { !it.isRead } }
    val clipboardManager = LocalClipboardManager.current
    var copiedAccount by remember { mutableStateOf(false) }

    val recentTransactions = remember(transactions) {
        transactions.take(4)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // 1. Top Header Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(EmeraldContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userProfile.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString(""),
                            fontWeight = FontWeight.Bold,
                            color = EmeraldDark,
                            fontSize = 17.sp
                        )
                    }
                    Column {
                        Text(
                            text = "Welcome Back 👋",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500
                        )
                        Text(
                            text = userProfile.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Admin Switch Button
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = EmeraldContainer,
                        modifier = Modifier.clickable {
                            viewModel.toggleAdminMode(true)
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "Admin",
                                tint = EmeraldDark,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Admin",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldDark
                            )
                        }
                    }

                    // Notification Bell with Badge
                    IconButton(
                        onClick = { viewModel.showNotificationsSheet.value = true },
                        modifier = Modifier.size(42.dp)
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadNotifications > 0) {
                                    Badge(
                                        containerColor = ErrorRed,
                                        contentColor = PureWhite
                                    ) {
                                        Text(text = unreadNotifications.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "Notifications",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }
        }

        // 2. Premium Banking Card (Total Balance)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(EmeraldDeep, EmeraldDark, EmeraldPrimary)
                            )
                        )
                        .padding(22.dp)
                ) {
                    Column {
                        // Card Top Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalance,
                                    contentDescription = "TrustBank",
                                    tint = SoftGold,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "TRUSTBANK PLATINUM",
                                    color = PureWhite.copy(alpha = 0.9f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }

                            // KYC Status Chip
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (userProfile.kycStatus == "VERIFIED") EmeraldLight.copy(alpha = 0.25f) else AmberGold.copy(alpha = 0.3f),
                                modifier = Modifier.clickable { viewModel.showKycSheet.value = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (userProfile.kycStatus == "VERIFIED") Icons.Default.CheckCircle else Icons.Default.Warning,
                                        contentDescription = "KYC Status",
                                        tint = if (userProfile.kycStatus == "VERIFIED") PureWhite else SoftGold,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = if (userProfile.kycStatus == "VERIFIED") "Verified" else "KYC Pending",
                                        color = PureWhite,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Balance row with hide/show toggle
                        Text(
                            text = "Total Balance",
                            color = PureWhite.copy(alpha = 0.75f),
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = if (userProfile.isHideBalance) "৳ •••••••" else "৳${String.format("%,.2f", userProfile.balance)}",
                                color = PureWhite,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            )
                            IconButton(
                                onClick = { viewModel.toggleHideBalance() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = if (userProfile.isHideBalance) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle Balance",
                                    tint = PureWhite.copy(alpha = 0.8f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Pending balance & Account number footer
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    text = "Account Number",
                                    color = PureWhite.copy(alpha = 0.7f),
                                    fontSize = 11.sp
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable {
                                        clipboardManager.setText(AnnotatedString(userProfile.accountNumber))
                                        copiedAccount = true
                                    }
                                ) {
                                    Text(
                                        text = userProfile.accountNumber,
                                        color = PureWhite,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy Account Number",
                                        tint = PureWhite.copy(alpha = 0.8f),
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }

                            if (userProfile.pendingBalance > 0.0) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = AmberGold.copy(alpha = 0.25f)
                                ) {
                                    Text(
                                        text = "Pending: ৳${String.format("%,.2f", userProfile.pendingBalance)}",
                                        color = SoftGold,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Quick Action Buttons (Add Money, Withdraw, Send, Receive)
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 18.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    QuickActionButton(
                        icon = Icons.Default.AddCard,
                        label = "Add Money",
                        bg = EmeraldContainer,
                        iconTint = EmeraldPrimary
                    ) {
                        viewModel.showAddMoneySheet.value = true
                    }

                    QuickActionButton(
                        icon = Icons.Default.Outbox,
                        label = "Withdraw",
                        bg = Color(0xFFFEF2F2),
                        iconTint = ErrorRed
                    ) {
                        viewModel.showWithdrawSheet.value = true
                    }

                    QuickActionButton(
                        icon = Icons.Default.Send,
                        label = "Send Money",
                        bg = Color(0xFFEFF6FF),
                        iconTint = InfoBlue
                    ) {
                        viewModel.showSendMoneySheet.value = true
                    }

                    QuickActionButton(
                        icon = Icons.Default.QrCode,
                        label = "Receive",
                        bg = Color(0xFFFAF5FF),
                        iconTint = Color(0xFF9333EA)
                    ) {
                        viewModel.showReceiveMoneySheet.value = true
                    }
                }
            }
        }

        // 4. KYC Alert Banner (if pending or unverified)
        if (userProfile.kycStatus != "VERIFIED") {
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clickable { viewModel.showKycSheet.value = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SoftGold)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(AmberGold),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = "KYC",
                                tint = PureWhite,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Complete KYC Verification",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Slate900
                            )
                            Text(
                                text = "Submit your NID/Passport to unlock unlimited deposit and withdrawal limits.",
                                fontSize = 12.sp,
                                color = Slate700
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Go",
                            tint = AmberGold
                        )
                    }
                }
            }
        }

        // 5. Monthly Transaction Trends Component (Lightweight Chart Visualization)
        item {
            Spacer(modifier = Modifier.height(14.dp))
            TransactionTrendsCard(
                monthlyTrends = monthlyTrends,
                onViewAllHistory = onNavigateToTransactions
            )
        }

        // 6. Promotional & Service Highlights
        item {
            Spacer(modifier = Modifier.height(18.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Featured Services",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ServiceCard(
                    modifier = Modifier.weight(1f),
                    title = "Instant bKash",
                    subtitle = "0% fee on deposits",
                    icon = Icons.Default.FlashOn,
                    iconBg = BkashPink
                ) {
                    viewModel.showAddMoneySheet.value = true
                }

                ServiceCard(
                    modifier = Modifier.weight(1f),
                    title = "Live Chat 24/7",
                    subtitle = "Smart AI Agent",
                    icon = Icons.Default.SupportAgent,
                    iconBg = EmeraldPrimary
                ) {
                    viewModel.showLiveChatDialog.value = true
                }
            }
        }

        // 6. Recent Transaction History
        item {
            Spacer(modifier = Modifier.height(22.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                TextButton(onClick = onNavigateToTransactions) {
                    Text(
                        text = "See All",
                        color = EmeraldPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        if (recentTransactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No recent transactions found.",
                            color = Slate500,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        } else {
            items(recentTransactions) { trx ->
                HomeTransactionItem(
                    transaction = trx,
                    onClick = { viewModel.selectedTransaction.value = trx }
                )
            }
        }
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    bg: Color,
    iconTint: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(bg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ServiceCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBg: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconBg,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = Slate500
                )
            }
        }
    }
}

@Composable
fun HomeTransactionItem(
    transaction: TransactionEntity,
    onClick: () -> Unit
) {
    val formattedDate = remember(transaction.timestamp) {
        val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
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
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = formattedDate,
                            fontSize = 11.sp,
                            color = Slate500
                        )
                        Text(
                            text = "•",
                            fontSize = 11.sp,
                            color = Slate400
                        )
                        Text(
                            text = transaction.trxId,
                            fontSize = 11.sp,
                            color = Slate500
                        )
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${if (isPositive) "+" else "-"}৳${String.format("%,.2f", transaction.amount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (isPositive) EmeraldDark else Slate900
                )
                Spacer(modifier = Modifier.height(3.dp))
                StatusBadge(transaction.status)
            }
        }
    }
}
