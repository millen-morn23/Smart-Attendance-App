package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

enum class UserRole {
    ADMIN,
    LECTURER,
    STUDENT
}

@Entity(tableName = "users")
data class User(
    @PrimaryKey val uid: String,
    val name: String,
    val email: String,
    val role: String, // ADMIN, LECTURER, STUDENT
    val department: String,
    val registerNo: String, // Matrix Number for student, Staff ID for lecturer
    val photoUrl: String? = null
) : Serializable

@Entity(tableName = "attendance_sessions")
data class AttendanceSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val courseCode: String,
    val courseTitle: String,
    val lecturerId: String,
    val lecturerName: String,
    val code: String, // 4-digit code
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Double,
    val status: String, // ACTIVE, CLOSED
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long
) : Serializable

@Entity(tableName = "attendance_records")
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val courseCode: String,
    val courseTitle: String,
    val studentId: String,
    val studentName: String,
    val registerNo: String,
    val timestamp: Long = System.currentTimeMillis(),
    val latitude: Double,
    val longitude: Double,
    val distanceMeters: Double,
    val status: String, // PRESENT, LATE, ABSENT
    val photoPath: String? = null,
    val isSynced: Boolean = false
) : Serializable

@Entity(tableName = "notifications")
data class NotificationLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
) : Serializable

@Entity(tableName = "sync_queue")
data class SyncItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recordId: Long,
    val actionType: String, // UPLOAD_RECORD, UPLOAD_PHOTO
    val retryCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "app_settings")
data class SettingsConfig(
    @PrimaryKey val key: String,
    val value: String
) : Serializable
