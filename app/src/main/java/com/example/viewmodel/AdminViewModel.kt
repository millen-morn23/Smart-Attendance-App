package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AttendanceRecord
import com.example.data.model.User
import com.example.data.repository.AttendanceRepository
import com.example.data.repository.UserRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AdminStats(
    val totalUsers: Int = 0,
    val totalSessions: Int = 0,
    val totalRecords: Int = 0,
    val onlineStatus: Boolean = true
)

class AdminViewModel(
    private val userRepository: UserRepository,
    private val attendanceRepository: AttendanceRepository
) : ViewModel() {

    // Manage Users
    val allUsers: StateFlow<List<User>> = userRepository.getAllUsersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Manage Records
    val allRecords: StateFlow<List<AttendanceRecord>> = attendanceRepository.allRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Admin Analytics Telemetry
    val adminStats: StateFlow<AdminStats> = combine(
        userRepository.getAllUsersFlow(),
        attendanceRepository.allSessions,
        attendanceRepository.allRecords,
        attendanceRepository.isOnline
    ) { users, sessions, records, online ->
        AdminStats(
            totalUsers = users.size,
            totalSessions = sessions.size,
            totalRecords = records.size,
            onlineStatus = online
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AdminStats())

    fun createNewUserByAdmin(
        uid: String,
        name: String,
        email: String,
        role: String,
        dept: String,
        regNo: String
    ) {
        viewModelScope.launch {
            val user = User(
                uid = uid.ifBlank { "uid_${System.currentTimeMillis()}" },
                name = name,
                email = email,
                role = role,
                department = dept,
                registerNo = regNo
            )
            userRepository.register(user)
        }
    }

    fun removeUser(user: User) {
        viewModelScope.launch {
            userRepository.deleteUser(user)
        }
    }

    fun purgeRecord(record: AttendanceRecord) {
        viewModelScope.launch {
            // Re-use editRecord with "ABSENT" or mark deleted conceptually
            val purged = record.copy(status = "ABSENT")
            attendanceRepository.editRecord(purged)
        }
    }

    fun pushGlobalSystemNotice(title: String, body: String) {
        viewModelScope.launch {
            // Distribute alert logs
            attendanceRepository.addNotification(
                userId = "student123",
                title = title,
                message = body
            )
            attendanceRepository.addNotification(
                userId = "lecturer123",
                title = title,
                message = body
            )
        }
    }
}
