package com.example.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        User::class,
        AttendanceSession::class,
        AttendanceRecord::class,
        NotificationLog::class,
        SyncItem::class,
        SettingsConfig::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun syncQueueDao(): SyncQueueDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smart_attendance_database"
                )
                    .addCallback(AppDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.userDao(), database.attendanceDao())
                }
            }
        }

        suspend fun populateDatabase(userDao: UserDao, attendanceDao: AttendanceDao) {
            // Seed Users
            val adminUser = User(
                uid = "admin123",
                name = "Dr. Susan Vance",
                email = "admin@university.edu",
                role = "ADMIN",
                department = "Administration Office",
                registerNo = "STAFF-001"
            )
            val lecturerUser = User(
                uid = "lecturer123",
                name = "Prof. Charles Xavier",
                email = "lecturer@university.edu",
                role = "LECTURER",
                department = "Computer Science & Engineering",
                registerNo = "STAFF-105"
            )
            val studentUser = User(
                uid = "student123",
                name = "John Doe",
                email = "student@university.edu",
                role = "STUDENT",
                department = "Computer Science",
                registerNo = "CS-2023-049"
            )
            val studentUser2 = User(
                uid = "student456",
                name = "Jane Smith",
                email = "jane@university.edu",
                role = "STUDENT",
                department = "Computer Science",
                registerNo = "CS-2023-088"
            )

            userDao.insertUser(adminUser)
            userDao.insertUser(lecturerUser)
            userDao.insertUser(studentUser)
            userDao.insertUser(studentUser2)

            // Seed Active Sessions
            val currentMillis = System.currentTimeMillis()
            val session1 = AttendanceSession(
                id = 1,
                courseCode = "MAD-401",
                courseTitle = "Mobile Application Development",
                lecturerId = "lecturer123",
                lecturerName = "Prof. Charles Xavier",
                code = "5821",
                latitude = 37.4220, // Near Android Emulator coordinates (Googleplex)
                longitude = -122.0841,
                radiusMeters = 50.0,
                status = "ACTIVE",
                createdAt = currentMillis,
                expiresAt = currentMillis + 7200000 // 2 hours
            )
            
            val session2 = AttendanceSession(
                id = 2,
                courseCode = "AI-302",
                courseTitle = "Artificial Intelligence & Neural Networks",
                lecturerId = "lecturer123",
                lecturerName = "Prof. Charles Xavier",
                code = "9940",
                latitude = 37.4227,
                longitude = -122.0845,
                radiusMeters = 30.0,
                status = "ACTIVE",
                createdAt = currentMillis - 3600000,
                expiresAt = currentMillis + 1800000 // 30 minutes left
            )

            val session3 = AttendanceSession(
                id = 3,
                courseCode = "SEC-412",
                courseTitle = "Advanced Cyber Security & Cryptography",
                lecturerId = "lecturer123",
                lecturerName = "Prof. Charles Xavier",
                code = "1122",
                latitude = 37.4215,
                longitude = -122.0835,
                radiusMeters = 100.0,
                status = "CLOSED", // Inactive Session
                createdAt = currentMillis - 86400000,
                expiresAt = currentMillis - 82800000
            )

            attendanceDao.insertSession(session1)
            attendanceDao.insertSession(session2)
            attendanceDao.insertSession(session3)

            // Seed Some Past Attendance Records for History
            val record1 = AttendanceRecord(
                sessionId = 3,
                courseCode = "SEC-412",
                courseTitle = "Advanced Cyber Security",
                studentId = "student123",
                studentName = "John Doe",
                registerNo = "CS-2023-049",
                timestamp = currentMillis - 84600000,
                latitude = 37.4214,
                longitude = -122.0834,
                distanceMeters = 12.5,
                status = "PRESENT",
                isSynced = true
            )
            val record2 = AttendanceRecord(
                sessionId = 3,
                courseCode = "SEC-412",
                courseTitle = "Advanced Cyber Security",
                studentId = "student456",
                studentName = "Jane Smith",
                registerNo = "CS-2023-088",
                timestamp = currentMillis - 84000000,
                latitude = 37.4216,
                longitude = -122.0836,
                distanceMeters = 8.2,
                status = "PRESENT",
                isSynced = true
            )

            attendanceDao.insertRecord(record1)
            attendanceDao.insertRecord(record2)

            // Seed system notification alerts
            val notification = NotificationLog(
                userId = "student123",
                title = "Welcome John!",
                message = "Welcome to the Smart Attendance System. View and mark active class lectures.",
                timestamp = currentMillis
            )
            val notification2 = NotificationLog(
                userId = "lecturer123",
                title = "Roster Active",
                message = "Class MAD-401 lecture session is marked ACTIVE.",
                timestamp = currentMillis
            )
            attendanceDao.insertNotification(notification)
            attendanceDao.insertNotification(notification2)
        }
    }
}
