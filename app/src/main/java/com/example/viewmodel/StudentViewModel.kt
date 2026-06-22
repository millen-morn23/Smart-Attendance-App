package com.example.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceSession
import com.example.data.model.NotificationLog
import com.example.data.repository.AttendanceRepository
import com.example.utils.LocationUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface StudentUiState {
    object Idle : StudentUiState
    object CheckingLocation : StudentUiState
    data class LocationVerified(val distance: Double, val allowed: Boolean) : StudentUiState
    object Submitting : StudentUiState
    data class Success(val record: AttendanceRecord) : StudentUiState
    data class Error(val message: String) : StudentUiState
}

class StudentViewModel(
    private val attendanceRepository: AttendanceRepository
) : ViewModel() {

    // Active attendance feeds open in classes
    val activeSessions: StateFlow<List<AttendanceSession>> = attendanceRepository.activeSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Simulated coordinate registers (defaults to standard Android emulator location coordinates)
    private val _studentLatitude = MutableStateFlow(37.4220)
    val studentLatitude: StateFlow<Double> = _studentLatitude.asStateFlow()

    private val _studentLongitude = MutableStateFlow(-122.0841)
    val studentLongitude: StateFlow<Double> = _studentLongitude.asStateFlow()

    private val _studentUiState = MutableStateFlow<StudentUiState>(StudentUiState.Idle)
    val studentUiState: StateFlow<StudentUiState> = _studentUiState.asStateFlow()

    private val _capturedPhotoPath = MutableStateFlow<String?>(null)
    val capturedPhotoPath: StateFlow<String?> = _capturedPhotoPath.asStateFlow()

    // History and synchronization signals
    val isOnline: Flow<Boolean> = attendanceRepository.isOnline

    fun setStudentCoordinates(lat: Double, lon: Double) {
        _studentLatitude.value = lat
        _studentLongitude.value = lon
    }

    fun setNetworkStatus(online: Boolean) {
        attendanceRepository.setOnlineStatus(online)
    }

    fun getStudentHistory(studentId: String): Flow<List<AttendanceRecord>> =
        attendanceRepository.getRecordsForStudent(studentId)

    fun getNotifications(studentId: String): Flow<List<NotificationLog>> =
        attendanceRepository.getNotificationsForUser(studentId)

    fun setCapturedPhoto(path: String?) {
        _capturedPhotoPath.value = path
    }

    fun verifyLocationAndSubmit(
        session: AttendanceSession,
        studentId: String,
        studentName: String,
        registerNo: String,
        enteredClassCode: String
    ) {
        _studentUiState.value = StudentUiState.CheckingLocation

        // 1. Verify Passcode
        if (session.code.trim() != enteredClassCode.trim()) {
            _studentUiState.value = StudentUiState.Error("Incorrect 4-digit class verification code entered. Expected '${session.code.trim()}', but got '${enteredClassCode.trim()}'.")
            return
        }

        viewModelScope.launch {
            // Calculate actual deviation
            val distance = LocationUtils.calculateDistance(
                lat1 = _studentLatitude.value,
                lon1 = _studentLongitude.value,
                lat2 = session.latitude,
                lon2 = session.longitude
            )
            val isAllowed = distance <= session.radiusMeters

            _studentUiState.value = StudentUiState.LocationVerified(distance, isAllowed)

            if (!isAllowed) {
                _studentUiState.value = StudentUiState.Error(
                    "GPS Verification Failed! You are outside the designated lecture radius. (Distance: ${String.format("%.1f", distance)}m, Max Allowed: ${session.radiusMeters}m)"
                )
                return@launch
            }

            // Location is verified! Submit the attendance record.
            _studentUiState.value = StudentUiState.Submitting
            
            val newRecord = AttendanceRecord(
                sessionId = session.id,
                courseCode = session.courseCode,
                courseTitle = session.courseTitle,
                studentId = studentId,
                studentName = studentName,
                registerNo = registerNo,
                latitude = _studentLatitude.value,
                longitude = _studentLongitude.value,
                distanceMeters = distance,
                status = "PRESENT",
                photoPath = _capturedPhotoPath.value,
                isSynced = false // Set by repository depending on connectivity
            )

            attendanceRepository.submitAttendanceRecord(newRecord)
                .onSuccess { syncedRecord ->
                    _studentUiState.value = StudentUiState.Success(syncedRecord)
                    // Reset single submission photo
                    _capturedPhotoPath.value = null
                }
                .onFailure { exception ->
                    _studentUiState.value = StudentUiState.Error(exception.localizedMessage ?: "Submission failed.")
                }
        }
    }

    fun resetState() {
        _studentUiState.value = StudentUiState.Idle
    }
}
