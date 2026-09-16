package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KycSheet(
    viewModel: BankingViewModel,
    onDismiss: () -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsState()

    var docType by remember { mutableStateOf(userProfile.kycDocumentType) }
    var docNumber by remember { mutableStateOf(userProfile.kycDocNumber) }
    var legalName by remember { mutableStateOf(userProfile.kycLegalName) }
    var dob by remember { mutableStateOf(userProfile.kycDateOfBirth) }
    var photoUploaded by remember { mutableStateOf(userProfile.kycStatus == "VERIFIED") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

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
                            text = "KYC Verification",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Verify your government-issued identity documents",
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

            // Current Status Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (userProfile.kycStatus == "VERIFIED") EmeraldContainer else SoftGold
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Current Status", fontSize = 11.sp, color = Slate600)
                            Text(
                                text = when (userProfile.kycStatus) {
                                    "VERIFIED" -> "Fully Verified Customer"
                                    "PENDING" -> "Document Under Compliance Review"
                                    else -> "Unverified Account"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (userProfile.kycStatus == "VERIFIED") EmeraldDark else Slate900
                            )
                        }
                        StatusBadge(userProfile.kycStatus)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Document Type Picker
            item {
                Text(text = "Identity Document Type", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Slate800)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("National ID (NID)", "Passport", "Driving License").forEach { type ->
                        val isSel = docType == type
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSel) EmeraldPrimary else Slate100,
                            modifier = Modifier.weight(1f).clickable { docType = type }
                        ) {
                            Box(modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (type.startsWith("National")) "NID" else if (type == "Passport") "Passport" else "License",
                                    color = if (isSel) PureWhite else Slate700,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Legal Name
            item {
                Text(text = "Full Legal Name (as on document)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Slate800)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = legalName,
                    onValueChange = { legalName = it },
                    placeholder = { Text("e.g. Md. Tanvir Mollah") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Document Number
            item {
                Text(text = "$docType Number", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Slate800)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = docNumber,
                    onValueChange = { docNumber = it },
                    placeholder = { Text("e.g. 1995829103859") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Date of Birth
            item {
                Text(text = "Date of Birth", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Slate800)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = dob,
                    onValueChange = { dob = it },
                    placeholder = { Text("e.g. 14 Aug 1995") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Document Photo Upload Box
            item {
                Text(text = "Document Front & Back Photo", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Slate800)
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = if (photoUploaded) EmeraldContainer else Slate50),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, if (photoUploaded) EmeraldPrimary else Slate300),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { photoUploaded = true }
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = if (photoUploaded) Icons.Default.CheckCircle else Icons.Default.CloudUpload,
                            contentDescription = null,
                            tint = if (photoUploaded) EmeraldPrimary else Slate400,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (photoUploaded) "Document scanned & attached (NID_FRONT_BACK.jpg)" else "Tap to capture or upload document photo",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (photoUploaded) EmeraldDark else Slate600
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            errorMsg?.let {
                item {
                    Text(text = it, color = ErrorRed, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            item {
                Button(
                    onClick = {
                        if (legalName.isBlank() || docNumber.isBlank()) {
                            errorMsg = "Please fill in legal name and document number"
                            return@Button
                        }
                        errorMsg = null
                        viewModel.submitKyc(docType, docNumber, legalName, dob)
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = PureWhite)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Submit for Verification", color = PureWhite, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecuritySheet(
    viewModel: BankingViewModel,
    onDismiss: () -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsState()
    var generatedOtp by remember { mutableStateOf<String?>(null) }
    var enteredOtp by remember { mutableStateOf("") }
    var otpVerified by remember { mutableStateOf(false) }

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
                            text = "Security Center",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Manage multi-factor authentication & hardware credentials",
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

            // OTP Verification Box
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate50),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Sms, contentDescription = null, tint = EmeraldPrimary)
                            Text(text = "One-Time Password (OTP) Verification", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Slate800)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Verify your registered mobile number (${userProfile.phone}) with an SMS OTP code.",
                            fontSize = 12.sp,
                            color = Slate600
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        if (generatedOtp == null) {
                            OutlinedButton(
                                onClick = {
                                    val otp = (1000..9999).random().toString()
                                    generatedOtp = otp
                                    viewModel.showSnackbar("SMS Sent: Your TrustBank verification code is $otp")
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Send Test SMS OTP")
                            }
                        } else {
                            Text(text = "Code sent: $generatedOtp (Simulated SMS)", fontSize = 12.sp, color = EmeraldDark, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = enteredOtp,
                                    onValueChange = { enteredOtp = it },
                                    placeholder = { Text("Enter OTP") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                                Button(
                                    onClick = {
                                        if (enteredOtp == generatedOtp) {
                                            otpVerified = true
                                            viewModel.showSnackbar("OTP verified successfully!")
                                        } else {
                                            viewModel.showSnackbar("Invalid OTP code. Please try again.")
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Verify", color = PureWhite)
                                }
                            }
                            if (otpVerified) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Device authenticated with OTP! ✅", color = EmeraldDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // New Device Verification Simulation
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate50),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Registered Trusted Devices", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Slate800)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "• Google Pixel 8 (This Device - Verified)\n• Chrome on Windows (Last active 2 hrs ago)", fontSize = 12.sp, color = Slate600, lineHeight = 18.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = {
                                viewModel.showSnackbar("Logged out all other 2 active sessions.")
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Logout All Other Devices")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    viewModel: BankingViewModel,
    onDismiss: () -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsState()
    var selectedLanguage by remember { mutableStateOf(userProfile.language) }
    var pushNotifications by remember { mutableStateOf(userProfile.pushNotificationsEnabled) }
    var smsAlerts by remember { mutableStateOf(userProfile.smsAlertsEnabled) }
    var maskAccounts by remember { mutableStateOf(userProfile.maskAccountNumbers) }
    var showTerms by remember { mutableStateOf(false) }
    var showPrivacy by remember { mutableStateOf(false) }

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
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Language Selector
            item {
                Text(text = "Language / ভাষা", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Slate800)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf("English", "বাংলা (Bengali)").forEach { lang ->
                        val isSel = selectedLanguage.startsWith(lang.take(2))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSel) EmeraldPrimary else Slate100,
                            modifier = Modifier.weight(1f).clickable {
                                selectedLanguage = lang
                                viewModel.showSnackbar("Language set to $lang")
                            }
                        ) {
                            Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                                Text(text = lang, color = if (isSel) PureWhite else Slate800, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }

            // Notification Settings
            item {
                Text(text = "Notification Preferences", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Slate800)
                Spacer(modifier = Modifier.height(6.dp))
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Slate50)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Push Notifications", fontSize = 13.sp, color = Slate800)
                            Switch(checked = pushNotifications, onCheckedChange = { pushNotifications = it })
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("SMS Transaction Alerts", fontSize = 13.sp, color = Slate800)
                            Switch(checked = smsAlerts, onCheckedChange = { smsAlerts = it })
                        }
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }

            // Privacy Settings
            item {
                Text(text = "Privacy Controls", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Slate800)
                Spacer(modifier = Modifier.height(6.dp))
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Slate50)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Mask Account Numbers", fontSize = 13.sp, color = Slate800)
                            Switch(checked = maskAccounts, onCheckedChange = { maskAccounts = it })
                        }
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }

            // Legal & Terms
            item {
                Text(text = "Legal", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Slate800)
                Spacer(modifier = Modifier.height(6.dp))
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Slate50)) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { showTerms = !showTerms }.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Terms & Conditions", fontSize = 13.sp, color = Slate800, fontWeight = FontWeight.Medium)
                            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Slate400)
                        }
                        if (showTerms) {
                            Text(
                                text = "TrustBank Fintech complies with Bangladesh Bank regulatory guidelines. All digital deposits and cashouts are secured via end-to-end encryption. Transaction PINs must not be shared.",
                                fontSize = 11.sp,
                                color = Slate600,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                        HorizontalDivider(color = Slate200)
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { showPrivacy = !showPrivacy }.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Privacy Policy", fontSize = 13.sp, color = Slate800, fontWeight = FontWeight.Medium)
                            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Slate400)
                        }
                        if (showPrivacy) {
                            Text(
                                text = "We value your privacy. Personal data including National ID numbers are stored in compliance with banking security standards. We never sell your personal information.",
                                fontSize = 11.sp,
                                color = Slate600,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
