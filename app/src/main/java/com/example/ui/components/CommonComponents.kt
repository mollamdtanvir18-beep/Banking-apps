package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.TransactionEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StatusBadge(status: String) {
    val (bgColor, textColor, label) = when (status.uppercase()) {
        "SUCCESSFUL", "APPROVED", "VERIFIED", "RESOLVED" -> Triple(
            EmeraldContainer,
            EmeraldDark,
            if (status == "APPROVED") "Approved" else if (status == "VERIFIED") "Verified" else "Successful"
        )
        "PENDING" -> Triple(
            SoftGold,
            AmberGold,
            "Pending"
        )
        "FAILED", "REJECTED" -> Triple(
            Color(0xFFFEE2E2),
            ErrorRed,
            if (status == "REJECTED") "Rejected" else "Failed"
        )
        else -> Triple(
            Slate100,
            Slate600,
            status
        )
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun MethodLogoBadge(method: String) {
    val (bg, icon, text) = when (method.lowercase()) {
        "bkash" -> Triple(BkashPink, Icons.Default.Payment, "bKash")
        "nagad" -> Triple(NagadOrange, Icons.Default.AccountBalanceWallet, "Nagad")
        "bank transfer", "bank account" -> Triple(BankNavy, Icons.Default.AccountBalance, "Bank")
        else -> Triple(EmeraldPrimary, Icons.Default.SwapHoriz, "TrustBank")
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = PureWhite,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
fun PinVerificationDialog(
    title: String = "Enter Transaction PIN",
    subtitle: String = "Please enter your 4-digit security PIN to authorize this transaction",
    onDismiss: () -> Unit,
    onPinEntered: (String) -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(EmeraldContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "PIN Lock",
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate500,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // PIN indicator dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until 4) {
                        val isFilled = i < pin.length
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(if (isFilled) EmeraldPrimary else Slate200)
                        )
                    }
                }

                errorMessage?.let {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = it, color = ErrorRed, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Keypad 1-9, C, 0, Backspace
                val keys = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("C", "0", "⌫")
                )

                for (row in keys) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (key in row) {
                            Surface(
                                shape = CircleShape,
                                color = when (key) {
                                    "C", "⌫" -> Slate100
                                    else -> Slate50
                                },
                                modifier = Modifier
                                    .size(60.dp)
                                    .padding(4.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        when (key) {
                                            "C" -> pin = ""
                                            "⌫" -> if (pin.isNotEmpty()) pin = pin.dropLast(1)
                                            else -> {
                                                if (pin.length < 4) {
                                                    val newPin = pin + key
                                                    pin = newPin
                                                    if (newPin.length == 4) {
                                                        onPinEntered(newPin)
                                                    }
                                                }
                                            }
                                        }
                                    }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Text(
                                        text = key,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate800
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel", color = Slate600, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun TransactionReceiptDialog(
    transaction: TransactionEntity,
    onDismiss: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    val formattedDate = remember(transaction.timestamp) {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        sdf.format(Date(transaction.timestamp))
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header badge
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(EmeraldContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (transaction.type) {
                            "DEPOSIT" -> Icons.Default.ArrowDownward
                            "WITHDRAWAL" -> Icons.Default.ArrowUpward
                            "SEND" -> Icons.Default.Send
                            else -> Icons.Default.CallReceived
                        },
                        contentDescription = "Transaction Type",
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "${transaction.type} RECEIPT",
                    style = MaterialTheme.typography.labelLarge,
                    color = EmeraldDark,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                val isPositive = transaction.type == "DEPOSIT" || transaction.type == "RECEIVE"
                Text(
                    text = "${if (isPositive) "+" else "-"}৳${String.format("%,.2f", transaction.amount)}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isPositive) EmeraldDark else Slate900
                )

                Spacer(modifier = Modifier.height(6.dp))
                StatusBadge(transaction.status)

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = Slate200, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // Detail Rows
                ReceiptRow(label = "Transaction ID", value = transaction.trxId, canCopy = true) {
                    clipboardManager.setText(AnnotatedString(transaction.trxId))
                    copied = true
                }
                ReceiptRow(label = "Method", value = transaction.method)
                ReceiptRow(label = "To / From", value = transaction.recipientOrSender)
                ReceiptRow(label = "Date & Time", value = formattedDate)
                if (transaction.fee > 0.0) {
                    ReceiptRow(label = "Transaction Fee", value = "৳${String.format("%,.2f", transaction.fee)}")
                }
                if (transaction.note.isNotBlank()) {
                    ReceiptRow(label = "Reference Note", value = transaction.note)
                }

                if (copied) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Transaction ID copied to clipboard!",
                        color = EmeraldPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Done", color = PureWhite, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ReceiptRow(
    label: String,
    value: String,
    canCopy: Boolean = false,
    onCopy: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Slate500, fontSize = 13.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                color = Slate800,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (canCopy) {
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(onClick = onCopy, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
