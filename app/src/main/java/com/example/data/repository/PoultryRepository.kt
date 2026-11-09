package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.data.backup.SheetsBackupData
import com.example.data.local.DailyReportEntity
import com.example.data.local.FarmProfileEntity
import com.example.data.local.MonthlyExpenseEntity
import com.example.data.local.RolePermissionConfig
import com.example.data.local.ShareholderEntity
import com.example.data.local.ShareholderPaymentEntity
import com.example.data.local.StaffEntity
import com.example.data.local.StaffPaymentEntity
import com.example.data.local.UserEntity
import com.example.domain.StockCalculationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class PoultryRepository(
    private val context: Context
) {
    private val TAG = "PoultryRepository"
    private val prefs: SharedPreferences = context.getSharedPreferences("kazi_agro_prefs", Context.MODE_PRIVATE)

    private val PREF_KEY_REMEMBER = "key_remember_login"
    private val PREF_KEY_REMEMBERED_EMAIL = "key_remembered_email"
    private val PREF_KEY_DARK_MODE = "key_dark_mode"
    private val PREF_KEY_FARM_NAME = "key_farm_name"
    private val PREF_KEY_OWNER_NAME = "key_owner_name"
    private val PREF_KEY_MOBILE = "key_mobile"
    private val PREF_KEY_ADDRESS = "key_address"
    private val PREF_KEY_LOGO_URI = "key_logo_uri"
    private val PREF_KEY_LOGO_EMOJI = "key_logo_emoji"
    private val PREF_KEY_AUTO_BACKUP = "key_auto_backup"
    private val PREF_KEY_INITIAL_STOCK = "key_initial_opening_stock"
    private val PREF_KEY_INITIAL_DATE = "key_initial_opening_date"
    private val PREF_KEY_IS_MANUAL_STOCK = "key_is_manual_stock"
    private val PREF_KEY_MANUAL_STOCK_COUNT = "key_manual_stock_count"

    // Helper date generator for realistic college presentation data
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private fun getPastDate(daysAgo: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
        return dateFormat.format(cal.time)
    }

    private fun getPastDateDisplay(daysAgo: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
        return displayDateFormat.format(cal.time)
    }

    // Default mock farm profile
    private val _farmProfile = MutableStateFlow(
        FarmProfileEntity(
            id = 1,
            farmName = prefs.getString(PREF_KEY_FARM_NAME, "কাজী এগ্রোটেক") ?: "কাজী এগ্রোটেক",
            ownerName = prefs.getString(PREF_KEY_OWNER_NAME, "মোঃ সাহারিয়ার রেদোয়ান") ?: "মোঃ সাহারিয়ার রেদোয়ান",
            mobileNumber = prefs.getString(PREF_KEY_MOBILE, "০১৭১২-৩৪৫৬৭৮") ?: "০১৭১২-৩৪৫৬৭৮",
            address = prefs.getString(PREF_KEY_ADDRESS, "খামার পাড়া, শ্রীপুর, গাজীপুর") ?: "খামার পাড়া, শ্রীপুর, গাজীপুর",
            logoUri = prefs.getString(PREF_KEY_LOGO_URI, "") ?: "",
            logoEmoji = prefs.getString(PREF_KEY_LOGO_EMOJI, "🐔") ?: "🐔",
            autoBackup = prefs.getBoolean(PREF_KEY_AUTO_BACKUP, true),
            isDarkMode = prefs.getBoolean(PREF_KEY_DARK_MODE, false),
            lastSyncTime = System.currentTimeMillis(),
            initialOpeningStock = prefs.getInt(PREF_KEY_INITIAL_STOCK, 1100),
            initialOpeningDate = prefs.getString(PREF_KEY_INITIAL_DATE, getPastDate(7)) ?: getPastDate(7),
            isManualStock = prefs.getBoolean(PREF_KEY_IS_MANUAL_STOCK, false),
            manualStockCount = prefs.getInt(PREF_KEY_MANUAL_STOCK_COUNT, 0)
        )
    )
    val farmProfile: Flow<FarmProfileEntity> = _farmProfile.asStateFlow()

    // Default demo users
    private val defaultUsers = listOf(
        UserEntity(
            id = "user_admin",
            username = "সাহারিয়ার রেদোয়ান",
            email = "admin@kaziagro.com",
            phone = "০১৭০০-১২৩৪৫৬",
            role = "ADMIN",
            isApproved = true,
            rememberLogin = true
        ),
        UserEntity(
            id = "user_manager",
            username = "আব্দুল করিম",
            email = "manager@kaziagro.com",
            phone = "০১৭১১-২২৩৩৪৪",
            role = "MANAGER",
            isApproved = true,
            rememberLogin = true
        ),
        UserEntity(
            id = "user_supervisor",
            username = "মোঃ রফিক",
            email = "supervisor@kaziagro.com",
            phone = "০১৮২২-৩৩৪৪৫৫",
            role = "SUPERVISOR",
            isApproved = true,
            rememberLogin = true
        ),
        UserEntity(
            id = "user_worker",
            username = "সুমন মিয়া",
            email = "worker@kaziagro.com",
            phone = "০১৯৩৩-৪৪৫৫৬৬",
            role = "WORKER",
            isApproved = true,
            rememberLogin = true
        )
    )

    private val _allUsers = MutableStateFlow<List<UserEntity>>(defaultUsers)
    val allUsers: Flow<List<UserEntity>> = _allUsers.asStateFlow()

    // Default logged in session (Admin)
    private val _currentUser = MutableStateFlow<UserEntity?>(defaultUsers[0])
    val currentUser: Flow<UserEntity?> = _currentUser.asStateFlow()

    // Dynamic Role Permissions
    private val _rolePermissions = MutableStateFlow<Map<String, RolePermissionConfig>>(
        mapOf(
            "ADMIN" to loadRolePermissionFromPrefs("ADMIN"),
            "MANAGER" to loadRolePermissionFromPrefs("MANAGER"),
            "SUPERVISOR" to loadRolePermissionFromPrefs("SUPERVISOR"),
            "WORKER" to loadRolePermissionFromPrefs("WORKER")
        )
    )
    val rolePermissions: Flow<Map<String, RolePermissionConfig>> = _rolePermissions.asStateFlow()

    // Pre-populated realistic Daily Reports
    private val _allDailyReports = MutableStateFlow<List<DailyReportEntity>>(
        listOf(
            DailyReportEntity(
                id = 1L,
                date = getPastDate(0), // Today
                currentBirds = 5200,
                deadBirds = 2,
                eggProduction = 4850,
                eggSold = 4700,
                eggPrice = 11.50,
                totalSale = 54050.0,
                medicineCost = 650.0,
                currentStock = 1250,
                remarks = "আজকের উৎপাদন স্বাভাবিক, পাইকারদের ডিম সরবরাহ সম্পন্ন।"
            ),
            DailyReportEntity(
                id = 2L,
                date = getPastDate(1),
                currentBirds = 5202,
                deadBirds = 3,
                eggProduction = 4820,
                eggSold = 4800,
                eggPrice = 11.50,
                totalSale = 55200.0,
                medicineCost = 400.0,
                currentStock = 1100,
                remarks = "শেড-১ ও শেড-২ এর ডিম সংগ্রহ ঠিক ছিল।"
            ),
            DailyReportEntity(
                id = 3L,
                date = getPastDate(2),
                currentBirds = 5205,
                deadBirds = 1,
                eggProduction = 4890,
                eggSold = 4750,
                eggPrice = 11.20,
                totalSale = 53200.0,
                medicineCost = 1200.0,
                currentStock = 1080,
                remarks = "ভিটামিন ও স্যালাইন স্প্রে করা হয়েছে।"
            ),
            DailyReportEntity(
                id = 4L,
                date = getPastDate(3),
                currentBirds = 5206,
                deadBirds = 4,
                eggProduction = 4780,
                eggSold = 4700,
                eggPrice = 11.50,
                totalSale = 54050.0,
                medicineCost = 0.0,
                currentStock = 940,
                remarks = "বাতাস চলাচলের জন্য ফ্যান সার্বক্ষণিক চালু ছিল।"
            ),
            DailyReportEntity(
                id = 5L,
                date = getPastDate(4),
                currentBirds = 5210,
                deadBirds = 2,
                eggProduction = 4840,
                eggSold = 4800,
                eggPrice = 11.50,
                totalSale = 55200.0,
                medicineCost = 850.0,
                currentStock = 860,
                remarks = "পানি শোধনকারী মেডিসিন ব্যবহার করা হয়েছে।"
            ),
            DailyReportEntity(
                id = 6L,
                date = getPastDate(5),
                currentBirds = 5212,
                deadBirds = 3,
                eggProduction = 4790,
                eggSold = 4750,
                eggPrice = 11.00,
                totalSale = 52250.0,
                medicineCost = 1500.0,
                currentStock = 820,
                remarks = "লেয়ার মুরগির নিয়মিত ভ্যাকসিন প্রয়োগ করা হয়েছে।"
            ),
            DailyReportEntity(
                id = 7L,
                date = getPastDate(6),
                currentBirds = 5215,
                deadBirds = 2,
                eggProduction = 4810,
                eggSold = 4700,
                eggPrice = 11.50,
                totalSale = 54050.0,
                medicineCost = 0.0,
                currentStock = 780,
                remarks = "শেড পরিষ্কার ও জীবাণুমুক্তকরণ সম্পন্ন।"
            )
        )
    )
    val allDailyReports: Flow<List<DailyReportEntity>> = _allDailyReports.asStateFlow()

    // Pre-populated realistic Monthly Expenses
    private val _allExpenses = MutableStateFlow<List<MonthlyExpenseEntity>>(
        listOf(
            DailyReportEntity(1L).let {
                MonthlyExpenseEntity(
                    id = 1L,
                    date = getPastDate(0),
                    feedCost = 85000.0,
                    medicineCost = 3500.0,
                    staffMarket = 2200.0,
                    totalExpense = 90700.0,
                    remarks = "লেয়ার গ্রোয়ার ফিড ৩০ বস্তা ও ভিটামিন ক্রয়।"
                )
            },
            MonthlyExpenseEntity(
                id = 2L,
                date = getPastDate(2),
                electricityBill = 14850.0,
                totalExpense = 14850.0,
                remarks = "চলতি মাসের পল্লী বিদ্যুৎ বিল পরিশোধ।"
            ),
            MonthlyExpenseEntity(
                id = 3L,
                date = getPastDate(3),
                vehicleRepair = 3200.0,
                staffMarket = 1500.0,
                totalExpense = 4700.0,
                remarks = "ডেলিভারি পিকআপ সার্ভিসিং ও মবিল ফিল্টার চেঞ্জ।"
            ),
            MonthlyExpenseEntity(
                id = 4L,
                date = getPastDate(5),
                staffSalary = 38000.0,
                totalExpense = 38000.0,
                remarks = "খামার স্টাফ ও কর্মীদের মাসিক বেতন পরিশোধ।"
            ),
            MonthlyExpenseEntity(
                id = 5L,
                date = getPastDate(6),
                feedCost = 92000.0,
                medicineCost = 4200.0,
                totalExpense = 96200.0,
                remarks = "কাজী ফিড ৫০ বস্তা ও অ্যান্টিবায়োটিক।"
            )
        )
    )
    val allExpenses: Flow<List<MonthlyExpenseEntity>> = _allExpenses.asStateFlow()

    // Pre-populated Shareholders
    private val _allShareholders = MutableStateFlow<List<ShareholderEntity>>(
        listOf(
            ShareholderEntity(id = "sh_1", name = "সাহারিয়ার রেদোয়ান (৪০%)", createdAt = System.currentTimeMillis() - 86400000L * 30),
            ShareholderEntity(id = "sh_2", name = "কাজী ইকবাল হোসেন (৩৫%)", createdAt = System.currentTimeMillis() - 86400000L * 30),
            ShareholderEntity(id = "sh_3", name = "তানভীর আহমেদ (২৫%)", createdAt = System.currentTimeMillis() - 86400000L * 30)
        )
    )
    val allShareholders: Flow<List<ShareholderEntity>> = _allShareholders.asStateFlow()

    // Pre-populated Shareholder Payments
    private val _allShareholderPayments = MutableStateFlow<List<ShareholderPaymentEntity>>(
        listOf(
            ShareholderPaymentEntity(
                id = "shp_1",
                shareholderId = "sh_1",
                shareholderName = "সাহারিয়ার রেদোয়ান (৪০%)",
                date = getPastDateDisplay(3),
                amount = 45000.0,
                paymentMethod = "Bank",
                note = "অক্টোবর মাসের লভ্যাংশ বন্টন"
            ),
            ShareholderPaymentEntity(
                id = "shp_2",
                shareholderId = "sh_2",
                shareholderName = "কাজী ইকবাল হোসেন (৩৫%)",
                date = getPastDateDisplay(3),
                amount = 39375.0,
                paymentMethod = "Bank",
                note = "অক্টোবর মাসের লভ্যাংশ বন্টন"
            ),
            ShareholderPaymentEntity(
                id = "shp_3",
                shareholderId = "sh_3",
                shareholderName = "তানভীর আহমেদ (২৫%)",
                date = getPastDateDisplay(3),
                amount = 28125.0,
                paymentMethod = "bKash",
                note = "অক্টোবর মাসের লভ্যাংশ বন্টন"
            )
        )
    )
    val allShareholderPayments: Flow<List<ShareholderPaymentEntity>> = _allShareholderPayments.asStateFlow()

    // Pre-populated Staff
    private val _allStaff = MutableStateFlow<List<StaffEntity>>(
        listOf(
            StaffEntity(id = "st_1", name = "মোঃ আব্দুল করিম", phone = "০১৭১১-২২৩৩৪৪", designation = "খামার ম্যানেজার"),
            StaffEntity(id = "st_2", name = "মোঃ রফিকুল ইসলাম", phone = "০১৮২২-৩৩৪৪৫৫", designation = "সুপারভাইজার"),
            StaffEntity(id = "st_3", name = "সুমন মিয়া", phone = "০১৯৩৩-৪৪৫৫৬৬", designation = "ফিডিং কর্মী")
        )
    )
    val allStaff: Flow<List<StaffEntity>> = _allStaff.asStateFlow()

    // Pre-populated Staff Payments
    private val _allStaffPayments = MutableStateFlow<List<StaffPaymentEntity>>(
        listOf(
            StaffPaymentEntity(
                id = "stp_1",
                staffId = "st_1",
                staffName = "মোঃ আব্দুল করিম",
                date = getPastDateDisplay(5),
                amount = 18000.0,
                paymentMethod = "Cash",
                note = "অক্টোবর মাসের পূর্ণ বেতন"
            ),
            StaffPaymentEntity(
                id = "stp_2",
                staffId = "st_2",
                staffName = "মোঃ রফিকুল ইসলাম",
                date = getPastDateDisplay(5),
                amount = 12000.0,
                paymentMethod = "Cash",
                note = "অক্টোবর মাসের পূর্ণ বেতন"
            ),
            StaffPaymentEntity(
                id = "stp_3",
                staffId = "st_3",
                staffName = "সুমন মিয়া",
                date = getPastDateDisplay(5),
                amount = 8000.0,
                paymentMethod = "Cash",
                note = "অক্টোবর মাসের পূর্ণ বেতন"
            )
        )
    )
    val allStaffPayments: Flow<List<StaffPaymentEntity>> = _allStaffPayments.asStateFlow()

    private fun loadRolePermissionFromPrefs(roleKey: String): RolePermissionConfig {
        val default = RolePermissionConfig.getDefaultPermissionsForRole(roleKey)
        val prefix = "role_perm_${roleKey.uppercase()}_"
        return RolePermissionConfig(
            roleKey = roleKey.uppercase(),
            roleDisplayName = default.roleDisplayName,
            dailyReportView = prefs.getBoolean("${prefix}daily_view", default.dailyReportView),
            dailyReportAdd = prefs.getBoolean("${prefix}daily_add", default.dailyReportAdd),
            userManagementView = prefs.getBoolean("${prefix}user_view", default.userManagementView),
            expenseView = prefs.getBoolean("${prefix}expense_view", default.expenseView),
            expenseAdd = prefs.getBoolean("${prefix}expense_add", default.expenseAdd),
            expenseDelete = prefs.getBoolean("${prefix}expense_delete", default.expenseDelete),
            reportAnalyticsView = prefs.getBoolean("${prefix}report_view", default.reportAnalyticsView),
            reportAnalyticsDownload = prefs.getBoolean("${prefix}report_download", default.reportAnalyticsDownload)
        )
    }

    private fun saveRolePermissionToPrefs(config: RolePermissionConfig) {
        val prefix = "role_perm_${config.roleKey.uppercase()}_"
        prefs.edit()
            .putBoolean("${prefix}daily_view", config.dailyReportView)
            .putBoolean("${prefix}daily_add", config.dailyReportAdd)
            .putBoolean("${prefix}user_view", config.userManagementView)
            .putBoolean("${prefix}expense_view", config.expenseView)
            .putBoolean("${prefix}expense_add", config.expenseAdd)
            .putBoolean("${prefix}expense_delete", config.expenseDelete)
            .putBoolean("${prefix}report_view", config.reportAnalyticsView)
            .putBoolean("${prefix}report_download", config.reportAnalyticsDownload)
            .apply()
    }

    private fun saveFarmProfileToPrefs(profile: FarmProfileEntity) {
        prefs.edit()
            .putString(PREF_KEY_FARM_NAME, profile.farmName)
            .putString(PREF_KEY_OWNER_NAME, profile.ownerName)
            .putString(PREF_KEY_MOBILE, profile.mobileNumber)
            .putString(PREF_KEY_ADDRESS, profile.address)
            .putString(PREF_KEY_LOGO_URI, profile.logoUri)
            .putString(PREF_KEY_LOGO_EMOJI, profile.logoEmoji)
            .putBoolean(PREF_KEY_AUTO_BACKUP, profile.autoBackup)
            .putBoolean(PREF_KEY_DARK_MODE, profile.isDarkMode)
            .putInt(PREF_KEY_INITIAL_STOCK, profile.initialOpeningStock)
            .putString(PREF_KEY_INITIAL_DATE, profile.initialOpeningDate)
            .putBoolean(PREF_KEY_IS_MANUAL_STOCK, profile.isManualStock)
            .putInt(PREF_KEY_MANUAL_STOCK_COUNT, profile.manualStockCount)
            .apply()
    }

    // Authentication & User Session Methods
    suspend fun signInWithEmail(email: String, pass: String, rememberMe: Boolean = true): Result<UserEntity> = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim()
        val matchedUser = _allUsers.value.find { it.email.equals(cleanEmail, ignoreCase = true) }
            ?: UserEntity(
                id = UUID.randomUUID().toString(),
                username = cleanEmail.substringBefore("@").replaceFirstChar { it.uppercase() },
                email = cleanEmail,
                role = if (cleanEmail.contains("admin", ignoreCase = true)) "ADMIN" else "MANAGER",
                isApproved = true,
                rememberLogin = rememberMe
            )

        if (!_allUsers.value.any { it.email.equals(cleanEmail, ignoreCase = true) }) {
            _allUsers.value = _allUsers.value + matchedUser
        }

        _currentUser.value = matchedUser
        if (rememberMe) {
            prefs.edit()
                .putBoolean(PREF_KEY_REMEMBER, true)
                .putString(PREF_KEY_REMEMBERED_EMAIL, cleanEmail)
                .apply()
        }
        Result.success(matchedUser)
    }

    suspend fun signUpWithEmail(email: String, pass: String, fullName: String, phone: String = "", rememberMe: Boolean = true): Result<UserEntity> = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim()
        val newUser = UserEntity(
            id = UUID.randomUUID().toString(),
            username = fullName.ifBlank { cleanEmail.substringBefore("@") },
            email = cleanEmail,
            phone = phone,
            role = if (cleanEmail.contains("admin", ignoreCase = true)) "ADMIN" else "WORKER",
            isApproved = true, // Auto approved for demo presentation
            rememberLogin = rememberMe
        )
        _allUsers.value = _allUsers.value + newUser
        _currentUser.value = newUser

        if (rememberMe) {
            prefs.edit()
                .putBoolean(PREF_KEY_REMEMBER, true)
                .putString(PREF_KEY_REMEMBERED_EMAIL, cleanEmail)
                .apply()
        }
        Result.success(newUser)
    }

    suspend fun adminAddUser(user: UserEntity): Result<Unit> = withContext(Dispatchers.IO) {
        val targetUser = if (user.id.isBlank()) user.copy(id = UUID.randomUUID().toString(), isApproved = true) else user
        _allUsers.value = _allUsers.value.filter { it.id != targetUser.id } + targetUser
        Result.success(Unit)
    }

    suspend fun adminUpdateUser(user: UserEntity): Result<Unit> = withContext(Dispatchers.IO) {
        _allUsers.value = _allUsers.value.map { if (it.id == user.id) user else it }
        if (_currentUser.value?.id == user.id) {
            _currentUser.value = user
        }
        Result.success(Unit)
    }

    suspend fun deleteUser(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        _allUsers.value = _allUsers.value.filter { it.id != userId }
        if (_currentUser.value?.id == userId) {
            signOut()
        }
        Result.success(Unit)
    }

    suspend fun updateCurrentUserProfile(
        name: String,
        phone: String,
        profileImageUri: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val curr = _currentUser.value ?: return@withContext Result.failure(Exception("No user logged in"))
        val updated = curr.copy(
            username = name,
            phone = phone,
            profileImageUri = profileImageUri ?: curr.profileImageUri
        )
        _currentUser.value = updated
        _allUsers.value = _allUsers.value.map { if (it.id == updated.id) updated else it }
        Result.success(Unit)
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        Result.success(Unit)
    }

    suspend fun changePassword(newPass: String): Result<Unit> = withContext(Dispatchers.IO) {
        Result.success(Unit)
    }

    fun signOut() {
        _currentUser.value = null
        prefs.edit().putBoolean(PREF_KEY_REMEMBER, false).apply()
    }

    fun isUserLoggedIn(): Boolean {
        return _currentUser.value != null
    }

    fun isUserLoggedInAndApproved(): Boolean {
        return _currentUser.value?.isApprovedUser() == true
    }

    fun isRememberLoginEnabled(): Boolean = prefs.getBoolean(PREF_KEY_REMEMBER, true)

    fun getRememberedEmail(): String = prefs.getString(PREF_KEY_REMEMBERED_EMAIL, "admin@kaziagro.com") ?: "admin@kaziagro.com"

    fun setRememberLogin(enabled: Boolean) {
        prefs.edit().putBoolean(PREF_KEY_REMEMBER, enabled).apply()
    }

    // Daily Report CRUD
    suspend fun saveDailyReport(report: DailyReportEntity): Long = withContext(Dispatchers.IO) {
        val targetId = if (report.id <= 0L) (System.currentTimeMillis() % 10000000L) else report.id
        val finalReport = report.copy(id = targetId, updatedAt = System.currentTimeMillis())

        val currentList = _allDailyReports.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == targetId || (it.date == finalReport.date && it.date.isNotBlank()) }
        if (index >= 0) {
            currentList[index] = finalReport
        } else {
            currentList.add(0, finalReport)
        }
        _allDailyReports.value = currentList.sortedByDescending { it.date }
        targetId
    }

    suspend fun getDailyReportById(id: Long): DailyReportEntity? = withContext(Dispatchers.IO) {
        _allDailyReports.value.find { it.id == id }
    }

    suspend fun deleteDailyReportById(id: Long) = withContext(Dispatchers.IO) {
        _allDailyReports.value = _allDailyReports.value.filter { it.id != id }
    }

    suspend fun getPreviousStock(date: String): Int = withContext(Dispatchers.IO) {
        val baselineStock = _farmProfile.value.initialOpeningStock
        StockCalculationService.calculateOpeningStockForDate(_allDailyReports.value, date, baselineStock)
    }

    suspend fun getLatestFlockCount(): Int = withContext(Dispatchers.IO) {
        val latest = _allDailyReports.value.firstOrNull()
        if (latest != null) {
            (latest.currentBirds - latest.deadBirds).coerceAtLeast(0)
        } else {
            5200
        }
    }

    // Monthly Expense CRUD
    suspend fun saveMonthlyExpense(expense: MonthlyExpenseEntity): Long = withContext(Dispatchers.IO) {
        val targetId = if (expense.id <= 0L) (System.currentTimeMillis() % 10000000L) else expense.id
        val finalExpense = expense.copy(id = targetId, updatedAt = System.currentTimeMillis())

        val currentList = _allExpenses.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == targetId }
        if (index >= 0) {
            currentList[index] = finalExpense
        } else {
            currentList.add(0, finalExpense)
        }
        _allExpenses.value = currentList.sortedByDescending { it.date }
        targetId
    }

    suspend fun getExpenseById(id: Long): MonthlyExpenseEntity? = withContext(Dispatchers.IO) {
        _allExpenses.value.find { it.id == id }
    }

    suspend fun deleteExpenseById(id: Long) = withContext(Dispatchers.IO) {
        _allExpenses.value = _allExpenses.value.filter { it.id != id }
    }

    // Farm Profile & Theme
    suspend fun updateFarmProfile(profile: FarmProfileEntity) = withContext(Dispatchers.IO) {
        _farmProfile.value = profile
        saveFarmProfileToPrefs(profile)
    }

    suspend fun updateDarkMode(isDark: Boolean) = withContext(Dispatchers.IO) {
        _farmProfile.value = _farmProfile.value.copy(isDarkMode = isDark)
        prefs.edit().putBoolean(PREF_KEY_DARK_MODE, isDark).apply()
    }

    suspend fun updateAutoBackup(enabled: Boolean) = withContext(Dispatchers.IO) {
        _farmProfile.value = _farmProfile.value.copy(autoBackup = enabled)
        prefs.edit().putBoolean(PREF_KEY_AUTO_BACKUP, enabled).apply()
    }

    // CSV Exports
    suspend fun exportDailyReportsToCsv(reports: List<DailyReportEntity>): File = withContext(Dispatchers.IO) {
        val file = File(context.cacheDir, "kazi_agrotech_daily_reports_${System.currentTimeMillis()}.csv")
        FileWriter(file).use { writer ->
            writer.append("তারিখ,বর্তমান মুরগী,মৃত মুরগী,ডিম উৎপাদন,বিক্রয় (ডিম),ডিমের দাম (৳),মোট বিক্রয় (৳),মন্তব্য\n")
            for (r in reports) {
                writer.append("${r.date},${r.currentBirds},${r.deadBirds},${r.eggProduction},${r.eggSold},${r.eggPrice},${r.totalSale},\"${r.remarks.replace("\"", "\"\"")}\"\n")
            }
        }
        file
    }

    suspend fun exportMonthlyExpensesToCsv(expenses: List<MonthlyExpenseEntity>): File = withContext(Dispatchers.IO) {
        val file = File(context.cacheDir, "kazi_agrotech_expenses_${System.currentTimeMillis()}.csv")
        FileWriter(file).use { writer ->
            writer.append("তারিখ,খাদ্য/ফিড (৳),ঔষধ ও ভ্যাকসিন (৳),স্টাফ বাজার (৳),স্টাফ বেতন (৳),গাড়ি মেরামত (৳),সম্পদ ক্রয় (৳),বিদ্যুৎ বিল (৳),অন্যান্য (৳),মোট ব্যয় (৳),মন্তব্য\n")
            for (e in expenses) {
                writer.append("${e.date},${e.feedCost},${e.medicineCost},${e.staffMarket},${e.staffSalary},${e.vehicleRepair},${e.assets},${e.electricityBill},${e.otherExpense},${e.totalExpense},\"${e.remarks.replace("\"", "\"\"")}\"\n")
            }
        }
        file
    }

    suspend fun updateRolePermissions(config: RolePermissionConfig) = withContext(Dispatchers.IO) {
        val currentMap = _rolePermissions.value.toMutableMap()
        currentMap[config.roleKey.uppercase()] = config
        _rolePermissions.value = currentMap
        saveRolePermissionToPrefs(config)
    }

    // Shareholder Operations
    suspend fun addShareholder(
        name: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) {
            onError("শেয়ারহোল্ডারের নাম প্রদান করুন")
            return@withContext
        }
        val entity = ShareholderEntity(id = "sh_${UUID.randomUUID()}", name = trimmed)
        _allShareholders.value = _allShareholders.value + entity
        onSuccess()
    }

    suspend fun updateShareholder(
        id: String,
        name: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) {
            onError("শেয়ারহোল্ডারের নাম খালি হতে পারবে না")
            return@withContext
        }
        _allShareholders.value = _allShareholders.value.map { if (it.id == id) it.copy(name = trimmed) else it }
        onSuccess()
    }

    suspend fun deleteShareholder(
        id: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        _allShareholders.value = _allShareholders.value.filter { it.id != id }
        onSuccess()
    }

    suspend fun addShareholderPayment(
        payment: ShareholderPaymentEntity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        val finalPayment = if (payment.id.isBlank()) payment.copy(id = "shp_${UUID.randomUUID()}") else payment
        _allShareholderPayments.value = listOf(finalPayment) + _allShareholderPayments.value
        onSuccess()
    }

    suspend fun updateShareholderPayment(
        payment: ShareholderPaymentEntity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        _allShareholderPayments.value = _allShareholderPayments.value.map { if (it.id == payment.id) payment else it }
        onSuccess()
    }

    suspend fun deleteShareholderPayment(
        id: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        _allShareholderPayments.value = _allShareholderPayments.value.filter { it.id != id }
        onSuccess()
    }

    // Staff Operations
    suspend fun addStaff(
        name: String,
        phone: String,
        onSuccess: (StaffEntity) -> Unit,
        onError: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) {
            onError("স্টাফের নাম প্রদান করুন")
            return@withContext
        }
        val entity = StaffEntity(id = "st_${UUID.randomUUID()}", name = trimmed, phone = phone.trim(), designation = "কর্মকর্তা")
        _allStaff.value = _allStaff.value + entity
        onSuccess(entity)
    }

    suspend fun updateStaff(
        id: String,
        name: String,
        phone: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) {
            onError("স্টাফের নাম খালি হতে পারবে না")
            return@withContext
        }
        _allStaff.value = _allStaff.value.map { if (it.id == id) it.copy(name = trimmed, phone = phone.trim()) else it }
        onSuccess()
    }

    suspend fun deleteStaff(
        id: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        _allStaff.value = _allStaff.value.filter { it.id != id }
        onSuccess()
    }

    suspend fun addStaffPayment(
        payment: StaffPaymentEntity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        val finalPayment = if (payment.id.isBlank()) payment.copy(id = "stp_${UUID.randomUUID()}") else payment
        _allStaffPayments.value = listOf(finalPayment) + _allStaffPayments.value
        onSuccess()
    }

    suspend fun updateStaffPayment(
        payment: StaffPaymentEntity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        _allStaffPayments.value = _allStaffPayments.value.map { if (it.id == payment.id) payment else it }
        onSuccess()
    }

    suspend fun deleteStaffPayment(
        id: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        _allStaffPayments.value = _allStaffPayments.value.filter { it.id != id }
        onSuccess()
    }

    // Backup & Restore
    suspend fun restoreCompleteBackup(content: SheetsBackupData) = withContext(Dispatchers.IO) {
        content.farmProfile?.let { _farmProfile.value = it }
        if (content.dailyReports.isNotEmpty()) _allDailyReports.value = content.dailyReports
        if (content.monthlyExpenses.isNotEmpty()) _allExpenses.value = content.monthlyExpenses
        if (content.users.isNotEmpty()) _allUsers.value = content.users
        if (content.rolePermissions.isNotEmpty()) _rolePermissions.value = content.rolePermissions
        if (content.shareholders.isNotEmpty()) _allShareholders.value = content.shareholders
        if (content.shareholderPayments.isNotEmpty()) _allShareholderPayments.value = content.shareholderPayments
        if (content.staff.isNotEmpty()) _allStaff.value = content.staff
        if (content.staffPayments.isNotEmpty()) _allStaffPayments.value = content.staffPayments
    }

    suspend fun fetchAllDataForBackup(): SheetsBackupData = withContext(Dispatchers.IO) {
        SheetsBackupData(
            farmProfile = _farmProfile.value,
            dailyReports = _allDailyReports.value,
            monthlyExpenses = _allExpenses.value,
            users = _allUsers.value,
            rolePermissions = _rolePermissions.value,
            shareholders = _allShareholders.value,
            shareholderPayments = _allShareholderPayments.value,
            staff = _allStaff.value,
            staffPayments = _allStaffPayments.value
        )
    }
}
