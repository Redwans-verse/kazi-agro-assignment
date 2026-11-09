package com.example.data.backup

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.data.repository.PoultryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class SheetsBackupWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val TAG = "SheetsBackupWorker"

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val manager = GoogleSheetsBackupManager(applicationContext)

        if (!manager.isAutoBackupEnabled()) {
            Log.d(TAG, "Auto Google Sheets backup is disabled. Skipping.")
            return@withContext Result.success()
        }

        val webAppUrl = manager.getWebAppUrl()
        if (webAppUrl.isBlank()) {
            Log.w(TAG, "Google Sheets Web App URL not configured. Skipping.")
            return@withContext Result.success()
        }

        if (!manager.isNetworkAvailable()) {
            Log.w(TAG, "No internet connection for Google Sheets backup. Will retry.")
            return@withContext Result.retry()
        }

        try {
            val repository = PoultryRepository(applicationContext)
            val backupData = repository.fetchAllDataForBackup()
            val userEmail = repository.getRememberedEmail().ifBlank { "admin@kaziagro.com" }

            val backupResult = manager.executeBackup(
                farmProfile = backupData.farmProfile,
                dailyReports = backupData.dailyReports,
                monthlyExpenses = backupData.monthlyExpenses,
                users = backupData.users,
                rolePermissions = backupData.rolePermissions,
                shareholders = backupData.shareholders,
                shareholderPayments = backupData.shareholderPayments,
                staff = backupData.staff,
                staffPayments = backupData.staffPayments,
                userId = "admin_user",
                userEmail = userEmail
            )

            if (backupResult.isSuccess) {
                Log.i(TAG, "Automatic Google Sheets backup succeeded: ${backupResult.getOrNull()?.message}")
                Result.success()
            } else {
                Log.e(TAG, "Automatic Google Sheets backup failed: ${backupResult.exceptionOrNull()?.message}")
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during Sheets auto backup: ${e.message}", e)
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "KaziAgroSheetsAutoBackupWork"

        fun schedule(context: Context, frequency: String = "1_HOUR") {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val intervalHours = when (frequency.uppercase()) {
                "1_HOUR", "1", "1_HOURS", "HOUR" -> 1L
                "6_HOURS", "6" -> 6L
                "12_HOURS", "12" -> 12L
                "DAILY", "24_HOURS", "24" -> 24L
                "MANUAL" -> {
                    cancel(context)
                    return
                }
                else -> 1L
            }

            val workRequest = PeriodicWorkRequestBuilder<SheetsBackupWorker>(
                intervalHours, TimeUnit.HOURS,
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    15, TimeUnit.MINUTES
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
            Log.i("SheetsBackupWorker", "Scheduled Sheets Auto Backup every $intervalHours hours")
        }

        fun runImmediate(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<SheetsBackupWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)
            Log.i("SheetsBackupWorker", "Triggered immediate Sheets Auto Backup")
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            Log.i("SheetsBackupWorker", "Cancelled Sheets Auto Backup")
        }
    }
}
