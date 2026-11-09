package com.example.data.update

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Data class representing update metadata.
 */
data class AppUpdateInfo(
    val versionCode: Int = 1,
    val versionName: String = "1.0.0",
    val apkUrl: String = "",
    val forceUpdate: Boolean = false,
    val title: String = "নতুন আপডেট পাওয়া গেছে",
    val message: String = "কাজী এগ্রোটেক-এর নতুন সংস্করণ পাওয়া গেছে।",
    val releaseNotes: String = "",
    val fileSize: Long = 0L,
    val sha256: String = "",
    val releaseDate: String = ""
)

/**
 * State of in-app update workflow.
 */
sealed class UpdateState {
    object Idle : UpdateState()
    object Checking : UpdateState()
    data class Available(val info: AppUpdateInfo) : UpdateState()
    data class Downloading(val progress: Float, val downloadedBytes: Long, val totalBytes: Long) : UpdateState()
    data class Downloaded(val apkFile: File, val info: AppUpdateInfo) : UpdateState()
    data class Installing(val info: AppUpdateInfo) : UpdateState()
    data class Error(val message: String) : UpdateState()
    object UpToDate : UpdateState()
}

/**
 * In-App Update Engine for Kazi Agrotech (Standalone Frontend Mode).
 */
class AppUpdateManager(private val context: Context) {

    private val TAG = "AppUpdateManager"

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    private val _availableUpdate = MutableStateFlow<AppUpdateInfo?>(null)
    val availableUpdate: StateFlow<AppUpdateInfo?> = _availableUpdate.asStateFlow()

    suspend fun checkForUpdates(
        isManual: Boolean = false,
        onResult: ((hasUpdate: Boolean, message: String?) -> Unit)? = null
    ): AppUpdateInfo? = withContext(Dispatchers.IO) {
        if (isManual) {
            _updateState.value = UpdateState.Checking
            kotlinx.coroutines.delay(300)
            _updateState.value = UpdateState.UpToDate
            withContext(Dispatchers.Main) {
                onResult?.invoke(false, "আপনি সর্বশেষ সংস্করণ (v1.0.0) ব্যবহার করছেন।")
            }
        }
        null
    }

    suspend fun downloadApk(
        info: AppUpdateInfo,
        onProgress: ((Float, Long, Long) -> Unit)? = null
    ): Result<File> = withContext(Dispatchers.IO) {
        Result.failure(Exception("অফলাইন সংস্করণে স্বয়ংক্রিয় আপডেট প্রযোজ্য নয়"))
    }

    fun installApk(apkFile: File, info: AppUpdateInfo? = null): Result<Unit> {
        return Result.success(Unit)
    }

    fun dismissUpdate() {
        _updateState.value = UpdateState.Idle
    }

    fun cancelDownload() {
        _updateState.value = UpdateState.Idle
    }
}
