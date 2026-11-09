package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BanglaNumberFormatter
import com.example.ui.components.FarmLogoDisplay
import com.example.ui.components.MainTopAppBar
import com.example.ui.components.SnackbarController
import com.example.ui.components.UserProfileAvatar
import com.example.ui.components.rememberHaptics
import com.example.ui.viewmodel.PoultryViewModel

@Composable
fun SettingsScreen(
    viewModel: PoultryViewModel,
    onNavigateToAdmin: () -> Unit = {},
    onNavigateToRolePermissions: (String) -> Unit = {},
    onNavigateToShareholderSettings: () -> Unit = {},
    onNavigateToStaffSettings: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToBackupRestore: () -> Unit = {},
    onOpenNotifications: () -> Unit = {},
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val haptics = rememberHaptics()
    val farmProfile by viewModel.farmProfile.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val dailyReports by viewModel.dailyReports.collectAsState()

    val todayDate = remember { BanglaNumberFormatter.getCurrentDateFormatted() }
    val hasTodayReport = remember(dailyReports, todayDate) { dailyReports.any { it.date == todayDate } }
    val notifDismissedDate by viewModel.notificationDismissedDate.collectAsState()
    val hasUnreadNotification = !hasTodayReport && notifDismissedDate != todayDate

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showLogoSelectionDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.uploadFarmLogoFromUri(
                context = context,
                imageUri = uri,
                onSuccess = {
                    showLogoSelectionDialog = false
                    SnackbarController.showMessage("লোগো ছবি সফলভাবে আপডেট করা হয়েছে!")
                },
                onError = { error ->
                    SnackbarController.showError("লোগো পরিবর্তন সমস্যা: $error")
                }
            )
        }
    }

    Scaffold(
        topBar = {
            MainTopAppBar(
                title = "সেটিংস",
                isRootScreen = true,
                logoUri = farmProfile.logoUri,
                logoEmoji = farmProfile.logoEmoji,
                userProfileImageUri = currentUser?.profileImageUri ?: "",
                username = currentUser?.username ?: "",
                hasUnreadNotification = hasUnreadNotification,
                onNotificationClick = onOpenNotifications,
                onProfileClick = onNavigateToProfile
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .testTag("settings_screen"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // User Profile Row (Quick Access)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onNavigateToProfile() }
                    .testTag("card_user_profile"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        UserProfileAvatar(
                            profileImageUri = currentUser?.profileImageUri ?: "",
                            username = currentUser?.username ?: "",
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentUser?.username ?: "অ্যাডমিন ব্যবহারকারী",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = "${currentUser?.email ?: "admin@kaziagro.com"} • ${currentUser?.roleNameBengali() ?: "অ্যাডমিন"}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = "View Profile",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Farm Profile Header Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { showEditProfileDialog = true }
                    .testTag("card_farm_profile"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable { showLogoSelectionDialog = true }
                            .padding(3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        FarmLogoDisplay(
                            logoUri = farmProfile.logoUri,
                            logoEmoji = farmProfile.logoEmoji,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = farmProfile.farmName,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                        Text(
                            text = "প্রোঃ ${farmProfile.ownerName}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                            )
                        )
                        Text(
                            text = "মোবাইলঃ ${farmProfile.mobileNumber}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        )
                        Text(
                            text = "ঠিকানাঃ ${farmProfile.address}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // General Settings Section
            Text(
                text = "অ্যাপ সেটিংস",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Dark Mode Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Default.DarkMode,
                                contentDescription = "Dark Mode",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "ডার্ক থিম (নাইট মোড)",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = "চোখের সুরক্ষায় ডার্ক কালার ব্যবহার",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }

                        Switch(
                            checked = farmProfile.isDarkMode,
                            onCheckedChange = { viewModel.toggleDarkMode(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    // Change Farm Logo
                    SettingsRowItem(
                        icon = Icons.Default.Image,
                        title = "খামারের লোগো নির্বাচন",
                        subtitle = "রিপোর্ট ও পিডিএফে প্রদর্শিত লোগো বা প্রতীক পরিবর্তন",
                        onClick = { showLogoSelectionDialog = true }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    // Shareholder Settings
                    SettingsRowItem(
                        icon = Icons.Default.Groups,
                        title = "শেয়ারহোল্ডার সেটিংস",
                        subtitle = "শেয়ারহোল্ডার তালিকা ও লভ্যাংশ ব্যবস্থাপনা",
                        onClick = onNavigateToShareholderSettings
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    // Staff Settings
                    SettingsRowItem(
                        icon = Icons.Default.Person,
                        title = "স্টাফ ও কর্মী সেটিংস",
                        subtitle = "খামারের কর্মী তালিকা ও বেতন ব্যবস্থাপনা",
                        onClick = onNavigateToStaffSettings
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    // Security / Change Password
                    SettingsRowItem(
                        icon = Icons.Default.Lock,
                        title = "পাসওয়ার্ড পরিবর্তন",
                        subtitle = "লগইন নিরাপত্তা ও পাসওয়ার্ড আপডেট",
                        onClick = { showChangePasswordDialog = true }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    // About App
                    SettingsRowItem(
                        icon = Icons.Default.Info,
                        title = "অ্যাপ পরিচিতি",
                        subtitle = "কাজী এগ্রোটেক সংস্করণ ১.০.০",
                        onClick = { showAboutDialog = true }
                    )
                }
            }

            // Logout Button
            Button(
                onClick = { showLogoutConfirm = true },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("btn_logout")
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "লগআউট করুন",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(72.dp))
        }
    }

    // Logo Selection Dialog
    if (showLogoSelectionDialog) {
        val logos = listOf(
            "🐔" to "পোল্ট্রি মুরগী",
            "🥚" to "ডিম আইকন",
            "🚜" to "ফার্ম ট্রাক্টর",
            "🌾" to "ধান ও গম",
            "🏡" to "খামার বাড়ি",
            "🐣" to "মুরগীর বাচ্চা",
            "🐓" to "মোরগ",
            "🏢" to "এগ্রোটেক কর্পোরেট"
        )
        AlertDialog(
            onDismissRequest = { showLogoSelectionDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "খামার লোগো নির্বাচন",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Upload Image Option
                    Button(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Upload",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("গ্যালারি থেকে ছবি আপলোড করুন", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Text(
                        text = "অথবা আইকন বেছে নিন:",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        logos.take(4).forEach { (emoji, _) ->
                            LogoOption(
                                emoji = emoji,
                                isSelected = farmProfile.logoUri.isBlank() && farmProfile.logoEmoji == emoji,
                                onSelect = {
                                    viewModel.updateFarmLogo(emoji)
                                    showLogoSelectionDialog = false
                                    SnackbarController.showMessage("লোগো আপডেট হয়েছে!")
                                }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        logos.drop(4).forEach { (emoji, _) ->
                            LogoOption(
                                emoji = emoji,
                                isSelected = farmProfile.logoUri.isBlank() && farmProfile.logoEmoji == emoji,
                                onSelect = {
                                    viewModel.updateFarmLogo(emoji)
                                    showLogoSelectionDialog = false
                                    SnackbarController.showMessage("লোগো আপডেট হয়েছে!")
                                }
                            )
                        }
                    }

                    if (farmProfile.logoUri.isNotBlank() || (farmProfile.logoEmoji.isNotBlank() && farmProfile.logoEmoji != "🐔")) {
                        OutlinedButton(
                            onClick = {
                                viewModel.resetToDefaultLogo {
                                    showLogoSelectionDialog = false
                                    SnackbarController.showMessage("ডিফল্ট লোগো রিসেট করা হয়েছে")
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("ডিফল্ট লোগো ফিরিয়ে আনুন", fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLogoSelectionDialog = false }) {
                    Text("বন্ধ করুন")
                }
            }
        )
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        var nameInput by remember { mutableStateOf(farmProfile.farmName) }
        var ownerInput by remember { mutableStateOf(farmProfile.ownerName) }
        var mobileInput by remember { mutableStateOf(farmProfile.mobileNumber) }
        var addressInput by remember { mutableStateOf(farmProfile.address) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("ফার্মের তথ্য পরিবর্তন", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("ফার্মের নাম") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = ownerInput,
                        onValueChange = { ownerInput = it },
                        label = { Text("মালিকের নাম") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = mobileInput,
                        onValueChange = { mobileInput = it },
                        label = { Text("মোবাইল নম্বর") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = addressInput,
                        onValueChange = { addressInput = it },
                        label = { Text("ঠিকানা") },
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        haptics.tap()
                        viewModel.updateFarmProfile(
                            farmName = nameInput,
                            ownerName = ownerInput,
                            mobileNumber = mobileInput,
                            address = addressInput,
                            logoEmoji = farmProfile.logoEmoji
                        )
                        showEditProfileDialog = false
                        SnackbarController.showMessage("ফার্মের তথ্য আপডেট সফল হয়েছে!")
                    }
                ) {
                    Text("সংরক্ষণ করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Change Password Dialog
    if (showChangePasswordDialog) {
        var newPass by remember { mutableStateOf("") }
        var confirmPass by remember { mutableStateOf("") }
        var errorMsg by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showChangePasswordDialog = false },
            title = { Text("পাসওয়ার্ড পরিবর্তন", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (errorMsg.isNotEmpty()) {
                        Text(
                            text = errorMsg,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    OutlinedTextField(
                        value = newPass,
                        onValueChange = {
                            newPass = it
                            errorMsg = ""
                        },
                        label = { Text("নতুন পাসওয়ার্ড") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = confirmPass,
                        onValueChange = {
                            confirmPass = it
                            errorMsg = ""
                        },
                        label = { Text("নতুন পাসওয়ার্ড পুনরায় লিখুন") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        haptics.tap()
                        if (newPass.isBlank()) {
                            errorMsg = "পাসওয়ার্ড খালি হতে পারবে না।"
                        } else if (newPass != confirmPass) {
                            errorMsg = "উভয় পাসওয়ার্ড মিলছে না।"
                        } else {
                            viewModel.changePassword(
                                newPass = newPass,
                                onSuccess = {
                                    showChangePasswordDialog = false
                                    SnackbarController.showMessage("পাসওয়ার্ড পরিবর্তন সফল হয়েছে!")
                                },
                                onError = { errorMsg = it }
                            )
                        }
                    }
                ) {
                    Text("সংরক্ষণ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePasswordDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("কাজী এগ্রোটেক ম্যানেজমেন্ট", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("সংস্করণ: ১.০.০")
                    Text("কাজী এগ্রোটেক পোল্ট্রি ফার্মের হিসাব ও ব্যবস্থাপনা সিস্টেম।")
                    Text("কলেজ অ্যাসাইনমেন্ট ও প্রজেক্ট প্রেজেন্টেশনের জন্য প্রস্তুতকৃত সংস্করণ।")
                    Text("প্রজেক্ট প্রস্তুতকারক: মোঃ সাহারিয়ার রেদোয়ান")
                }
            },
            confirmButton = {
                Button(onClick = { showAboutDialog = false }) {
                    Text("ঠিক আছে")
                }
            }
        )
    }

    // Logout Confirm Dialog
    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("লগআউট নিশ্চিতকরণ", fontWeight = FontWeight.Bold) },
            text = { Text("আপনি কি নিশ্চিতভাবে অ্যাকাউন্ট থেকে লগআউট করতে চান?") },
            confirmButton = {
                Button(
                    onClick = {
                        haptics.confirm()
                        showLogoutConfirm = false
                        viewModel.logout {
                            onLogout()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("লগআউট")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

@Composable
fun LogoOption(
    emoji: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(54.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onSelect() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = emoji, fontSize = 28.sp)
    }
}

@Composable
fun SettingsRowItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = "Go",
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
