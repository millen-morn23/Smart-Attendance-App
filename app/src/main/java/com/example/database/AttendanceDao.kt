package com.example.database

import androidx.room.*
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceSession
import com.example.data.model.NotificationLog
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    // --- Sessions ---
    @Query("SELECT * FROM attendance_sessions ORDER BY createdAt DESC")
    fun getAllSessionsFlow(): Flow<List<AttendanceSession>>

    @Query("SELECT * FROM attendance_sessions WHERE status = 'ACTIVE' ORDER BY createdAt DESC")
    fun getActiveSessionsFlow(): Flow<List<AttendanceSession>>

    @Query("SELECT * FROM attendance_sessions WHERE id = :id LIMIT 1")
    suspend fun getSessionById(id: Long): AttendanceSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: AttendanceSession): Long

    @Update
    suspend fun updateSession(session: AttendanceSession)

    // --- Records ---
    @Query("SELECT * FROM attendance_records WHERE sessionId = :sessionId ORDER BY timestamp DESC")
    fun getRecordsForSessionFlow(sessionId: Long): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE studentId = :studentId ORDER BY timestamp DESC")
    fun getRecordsForStudentFlow(studentId: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records ORDER BY timestamp DESC")
    fun getAllRecordsFlow(): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE sessionId = :sessionId AND studentId = :studentId LIMIT 1")
    suspend fun getRecordForStudentInSession(sessionId: Long, studentId: String): AttendanceRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: AttendanceRecord): Long

    @Update
    suspend fun updateRecord(record: AttendanceRecord)

    // --- Notifications ---
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY timestamp DESC")
    fun getNotificationsForUserFlow(userId: String): Flow<List<NotificationLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationLog): Long

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllNotificationsAsRead(userId: String)

    // --- Stats ---
    @Query("SELECT COUNT(*) FROM attendance_sessions")
    fun getSessionsCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM attendance_records")
    fun getRecordsCountFlow(): Flow<Int>
}
