package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.repository.AttendanceRepository
import com.example.data.repository.UserRepository

@Suppress("UNCHECKED_CAST")
class ViewModelFactory(
    private val userRepository: UserRepository,
    private val attendanceRepository: AttendanceRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(userRepository) as T
            }
            modelClass.isAssignableFrom(StudentViewModel::class.java) -> {
                StudentViewModel(attendanceRepository) as T
            }
            modelClass.isAssignableFrom(LecturerViewModel::class.java) -> {
                LecturerViewModel(attendanceRepository) as T
            }
            modelClass.isAssignableFrom(AdminViewModel::class.java) -> {
                AdminViewModel(userRepository, attendanceRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
