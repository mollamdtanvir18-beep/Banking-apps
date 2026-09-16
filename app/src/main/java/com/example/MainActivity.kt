package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.TransactionEntity
import com.example.ui.BankingViewModel
import com.example.ui.BankingViewModelFactory
import com.example.ui.NavigationTab
import com.example.ui.components.TransactionReceiptDialog
import com.example.ui.screens.*
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val factory = BankingViewModelFactory(application)

        setContent {
            MyApplicationTheme {
                val viewModel: BankingViewModel = viewModel(factory = factory)
                BankingApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun BankingApp(viewModel: BankingViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val isAdminMode by viewModel.isAdminMode.collectAsState()

    // Sheet and dialog states collected as State
    val showAddMoney by viewModel.showAddMoneySheet.collectAsState()
    val showWithdraw by viewModel.showWithdrawSheet.collectAsState()
    val showSendMoney by viewModel.showSendMoneySheet.collectAsState()
    val showReceiveMoney by viewModel.showReceiveMoneySheet.collectAsState()
    val showNotifications by viewModel.showNotificationsSheet.collectAsState()
    val showKyc by viewModel.showKycSheet.collectAsState()
    val showSecurity by viewModel.showSecuritySheet.collectAsState()
    val showSettings by viewModel.showSettingsSheet.collectAsState()
    val showLiveChat by viewModel.showLiveChatDialog.collectAsState()
    val showReportProblem by viewModel.showReportProblemDialog.collectAsState()
    val selectedTransaction: TransactionEntity? by viewModel.selectedTransaction.collectAsState()

    // Snackbar Host State
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (!isAdminMode) {
                BankingBottomNavigationBar(
                    currentTab = currentTab,
                    onTabSelected = { tab -> viewModel.selectTab(tab) }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isAdminMode) {
                AdminPanelScreen(viewModel = viewModel)
            } else {
                Crossfade(targetState = currentTab, label = "TabCrossfade") { tab ->
                    when (tab) {
                        NavigationTab.HOME -> HomeScreen(
                            viewModel = viewModel,
                            onNavigateToTransactions = { viewModel.selectTab(NavigationTab.TRANSACTIONS) }
                        )
                        NavigationTab.WALLET -> WalletScreen(viewModel = viewModel)
                        NavigationTab.TRANSACTIONS -> TransactionsScreen(viewModel = viewModel)
                        NavigationTab.SUPPORT -> SupportScreen(viewModel = viewModel)
                        NavigationTab.PROFILE -> ProfileScreen(viewModel = viewModel)
                        NavigationTab.ADMIN -> AdminPanelScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }

    // Modal Bottom Sheets & Dialogs
    if (showAddMoney) {
        AddMoneySheet(
            viewModel = viewModel,
            onDismiss = { viewModel.showAddMoneySheet.value = false }
        )
    }

    if (showWithdraw) {
        WithdrawSheet(
            viewModel = viewModel,
            onDismiss = { viewModel.showWithdrawSheet.value = false }
        )
    }

    if (showSendMoney) {
        SendMoneySheet(
            viewModel = viewModel,
            onDismiss = { viewModel.showSendMoneySheet.value = false }
        )
    }

    if (showReceiveMoney) {
        ReceiveMoneySheet(
            viewModel = viewModel,
            onDismiss = { viewModel.showReceiveMoneySheet.value = false }
        )
    }

    if (showNotifications) {
        NotificationsSheet(
            viewModel = viewModel,
            onDismiss = { viewModel.showNotificationsSheet.value = false }
        )
    }

    if (showKyc) {
        KycSheet(
            viewModel = viewModel,
            onDismiss = { viewModel.showKycSheet.value = false }
        )
    }

    if (showSecurity) {
        SecuritySheet(
            viewModel = viewModel,
            onDismiss = { viewModel.showSecuritySheet.value = false }
        )
    }

    if (showSettings) {
        SettingsSheet(
            viewModel = viewModel,
            onDismiss = { viewModel.showSettingsSheet.value = false }
        )
    }

    if (showLiveChat) {
        LiveChatDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.showLiveChatDialog.value = false }
        )
    }

    if (showReportProblem) {
        ReportProblemDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.showReportProblemDialog.value = false }
        )
    }

    selectedTransaction?.let { trx ->
        TransactionReceiptDialog(
            transaction = trx,
            onDismiss = { viewModel.selectedTransaction.value = null }
        )
    }
}

@Composable
fun BankingBottomNavigationBar(
    currentTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        modifier = Modifier.height(72.dp)
    ) {
        val navItems = listOf(
            NavigationItemData(NavigationTab.HOME, "Home", Icons.Filled.Home, Icons.Outlined.Home),
            NavigationItemData(NavigationTab.WALLET, "Wallet", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet),
            NavigationItemData(NavigationTab.TRANSACTIONS, "History", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong),
            NavigationItemData(NavigationTab.SUPPORT, "Support", Icons.Filled.Headphones, Icons.Outlined.Headphones),
            NavigationItemData(NavigationTab.PROFILE, "Profile", Icons.Filled.Person, Icons.Outlined.Person)
        )

        navItems.forEach { item ->
            val isSelected = currentTab == item.tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(item.tab) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 11.sp
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = EmeraldPrimary,
                    selectedTextColor = EmeraldPrimary,
                    indicatorColor = EmeraldContainer,
                    unselectedIconColor = Slate400,
                    unselectedTextColor = Slate500
                )
            )
        }
    }
}

private data class NavigationItemData(
    val tab: NavigationTab,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)
