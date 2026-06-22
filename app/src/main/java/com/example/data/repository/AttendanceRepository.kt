package com.example.data.repository

import android.util.Log
import com.example.data.model.*
import com.example.database.AttendanceDao
import com.example.database.SyncQueueDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AttendanceRepository(
    private val attendanceDao: AttendanceDao,
    private val syncQueueDao: SyncQueueDao
) {
    // Flows observed by ViewModels
    val allSessions: Flow<List<AttendanceSession>> = attendanceDao.getAllSessionsFlow()
    val activeSessions: Flow<List<AttendanceSession>> = attendanceDao.getActiveSessionsFlow()
    val allRecords: Flow<List<AttendanceRecord>> = attendanceDao.getAllRecordsFlow()

    // Simulated cloud connection state
    private val _isOnline = MutableStateFlow(true)
    val isOnline: Flow<Boolean> = _isOnline.asStateFlow()

    fun setOnlineStatus(online: Boolean) {
        _isOnline.value = online
        if (online) {
            // Trigger auto-sync in standard background thread
            kotlinx.coroutines.GlobalScope.launch {
                syncPendingItems()
            }
        }
    }

    suspend fun createSession(session: AttendanceSession): Long {
        return attendanceDao.insertSession(session)
    }

    suspend fun getSessionById(sessionId: Long): AttendanceSession? {
        return attendanceDao.getSessionById(sessionId)
    }

    suspend fun submitAttendanceRecord(record: AttendanceRecord): Result<AttendanceRecord> {
        // 1. Check if student already submitted to prevent double submissions
        val existing = attendanceDao.getRecordForStudentInSession(record.sessionId, record.studentId)
        if (existing != null) {
            return Result.failure(Exception("Attendance has already been recorded for this lecture!"))
        }

        // 2. Insert into room database initially (Offline-first source of truth)
        val insertedId = attendanceDao.insertRecord(record)
        val savedRecord = record.copy(id = insertedId)

        // 3. Check connectivity. If offline, append to the outbox queue.
        if (!_isOnline.value) {
            syncQueueDao.insertSyncItem(
                SyncItem(
                    recordId = insertedId,
                    actionType = "UPLOAD_RECORD"
                )
            )
            Log.d("AttendanceRepository", "Offline: Attendance cached in sync queue.")
            return Result.success(savedRecord)
        } else {
            // Online: Attempt direct Firebase sync.
            try {
                // Simulate quick API network delay
                kotlinx.coroutines.delay(1000)
                val syncedRecord = savedRecord.copy(isSynced = true)
                attendanceDao.updateRecord(syncedRecord)
                
                // Add success notification log
                attendanceDao.insertNotification(
                    NotificationLog(
                        userId = record.studentId,
                        title = "Attendance Checked-In!",
                        message = "Your check-in for course ${record.courseCode} was successfully logged and synchronized with academic servers.",
                    )
                )
                return Result.success(syncedRecord)
            } catch (e: Exception) {
                // Network fail: Fallback to local outbox queue
                syncQueueDao.insertSyncItem(
                    SyncItem(
                        recordId = insertedId,
                        actionType = "UPLOAD_RECORD"
                    )
                )
                return Result.success(savedRecord)
            }
        }
    }

    suspend fun editRecord(record: AttendanceRecord) {
        attendanceDao.updateRecord(record)
        // Sync modified status back to simulated remote
        if (_isOnline.value) {
            attendanceDao.updateRecord(record.copy(isSynced = true))
        } else {
            syncQueueDao.insertSyncItem(SyncItem(recordId = record.id, actionType = "UPLOAD_RECORD"))
        }
    }

    fun getRecordsForSession(sessionId: Long): Flow<List<AttendanceRecord>> =
        attendanceDao.getRecordsForSessionFlow(sessionId)

    fun getRecordsForStudent(studentId: String): Flow<List<AttendanceRecord>> =
        attendanceDao.getRecordsForStudentFlow(studentId)

    fun getNotificationsForUser(userId: String): Flow<List<NotificationLog>> =
        attendanceDao.getNotificationsForUserFlow(userId)

    suspend fun markNotificationsRead(userId: String) {
        attendanceDao.markAllNotificationsAsRead(userId)
    }

    suspend fun addNotification(userId: String, title: String, message: String) {
        attendanceDao.insertNotification(
            NotificationLog(
                userId = userId,
                title = title,
                message = message
            )
        )
    }

    // --- Outbox Sync Engine ---
    suspend fun syncPendingItems(): Int {
        val items = syncQueueDao.getPendingSyncItems()
        if (items.isEmpty()) return 0
        Log.d("AttendanceRepository", "Sync Engine: Syncing ${items.size} pending items...")

        var syncCount = 0
        for (item in items) {
            try {
                // Retrieve record from sqlite
                val recordId = item.recordId
                // We'll sync by updating isSynced flag to database directly
                // Simulate cloud upload API wait time
                kotlinx.coroutines.delay(500)
                
                // Find and update sync tag in the main DB
                val records = attendanceDao.getAllRecordsFlow().first()
                val target = records.find { it.id == recordId }
                if (target != null) {
                    attendanceDao.updateRecord(target.copy(isSynced = true))
                    // Notify student of sync status success
                    attendanceDao.insertNotification(
                        NotificationLog(
                            userId = target.studentId,
                            title = "Sync Complete",
                            message = "Pending attendance record for ${target.courseCode} has successfully sync-uploaded to student records.",
                        )
                    )
                }
                syncQueueDao.deleteSyncItem(item)
                syncCount++
            } catch (e: Exception) {
                Log.e("AttendanceRepository", "Sync failed for item ${item.id}: ${e.message}")
            }
        }
        return syncCount
    }
}
