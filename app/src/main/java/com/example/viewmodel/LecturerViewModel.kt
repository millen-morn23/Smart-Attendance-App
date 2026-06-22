package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceSession
import com.example.data.repository.AttendanceRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LecturerViewModel(
    private val attendanceRepository: AttendanceRepository
) : ViewModel() {

    // All university sessions
    val allSessions: StateFlow<List<AttendanceSession>> = attendanceRepository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _createdSessionId = MutableStateFlow<Long?>(null)
    val createdSessionId: StateFlow<Long?> = _createdSessionId.asStateFlow()

    fun getSessionsForLecturer(lecturerId: String): Flow<List<AttendanceSession>> =
        attendanceRepository.allSessions.map { list ->
            list.filter { it.lecturerId == lecturerId }
        }

    fun getRecordsForSession(sessionId: Long): Flow<List<AttendanceRecord>> =
        attendanceRepository.getRecordsForSession(sessionId)

    fun createAttendanceSession(
        courseCode: String,
        courseTitle: String,
        lecturerId: String,
        lecturerName: String,
        passcode: String,
        latitude: Double,
        longitude: Double,
        radiusMeters: Double,
        durationMinutes: Int
    ) {
        viewModelScope.launch {
            val trimmedPasscode = passcode.trim()
            val finalCode = trimmedPasscode.ifBlank { (1000..9999).random().toString() }
            val session = AttendanceSession(
                courseCode = courseCode,
                courseTitle = courseTitle,
                lecturerId = lecturerId,
                lecturerName = lecturerName,
                code = finalCode,
                latitude = latitude,
                longitude = longitude,
                radiusMeters = radiusMeters,
                status = "ACTIVE",
                expiresAt = System.currentTimeMillis() + (durationMinutes * 60000)
            )
            val id = attendanceRepository.createSession(session)
            _createdSessionId.value = id
            
            // Broadcast systems update to both seed students
            attendanceRepository.addNotification(
                userId = "student123",
                title = "New Lecture Session!",
                message = "Prof. Charles Xavier opened attendance for $courseCode. Access passcode is '$finalCode'. Check in before it closes."
            )
            attendanceRepository.addNotification(
                userId = "student456",
                title = "New Lecture Session!",
                message = "Prof. Charles Xavier opened attendance for $courseCode. Access passcode is '$finalCode'. Check in before it closes."
            )
        }
    }

    fun updateSessionStatus(session: AttendanceSession, status: String) {
        viewModelScope.launch {
            val updated = session.copy(status = status)
            attendanceRepository.createSession(updated) // REPLACE conflict inserts
            
            // Clear creation logs
            if (status == "CLOSED") {
                _createdSessionId.value = null
            }
        }
    }

    fun modifyStudentRecordStatus(record: AttendanceRecord, newStatus: String) {
        viewModelScope.launch {
            val updated = record.copy(status = newStatus)
            attendanceRepository.editRecord(updated)
        }
    }

    fun resetCreationState() {
        _createdSessionId.value = null
    }
}
