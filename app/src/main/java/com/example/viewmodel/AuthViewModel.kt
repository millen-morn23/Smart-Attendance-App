package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.User
import com.example.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    object Idle : AuthUiState
    object Loading : AuthUiState
    data class Success(val user: User) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class AuthViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    val currentUser: StateFlow<User?> = userRepository.currentUser

    private val _authUiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val authUiState: StateFlow<AuthUiState> = _authUiState.asStateFlow()

    private val _forgotPasswordState = MutableStateFlow<String?>(null)
    val forgotPasswordState: StateFlow<String?> = _forgotPasswordState.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authUiState.value = AuthUiState.Error("Email and password fields cannot be empty.")
            return
        }
        _authUiState.value = AuthUiState.Loading
        viewModelScope.launch {
            userRepository.login(email, password)
                .onSuccess { user ->
                    _authUiState.value = AuthUiState.Success(user)
                }
                .onFailure { exception ->
                    _authUiState.value = AuthUiState.Error(exception.localizedMessage ?: "Login failed.")
                }
        }
    }

    fun register(uid: String, name: String, email: String, role: String, department: String, registerNo: String) {
        if (name.isBlank() || email.isBlank() || registerNo.isBlank()) {
            _authUiState.value = AuthUiState.Error("Please fill in all mandatory registry fields.")
            return
        }
        _authUiState.value = AuthUiState.Loading
        viewModelScope.launch {
            val newUser = User(
                uid = uid.ifBlank { "uid_${System.currentTimeMillis()}" },
                name = name,
                email = email,
                role = role,
                department = department,
                registerNo = registerNo
            )
            userRepository.register(newUser)
                .onSuccess { user ->
                    _authUiState.value = AuthUiState.Success(user)
                }
                .onFailure { exception ->
                    _authUiState.value = AuthUiState.Error(exception.localizedMessage ?: "Registration failed.")
                }
        }
    }

    fun forgotPassword(email: String) {
        if (email.isBlank()) {
            _forgotPasswordState.value = "Please enter your enrolled email address."
            return
        }
        viewModelScope.launch {
            userRepository.forgotPassword(email)
                .onSuccess { msg ->
                    _forgotPasswordState.value = msg
                }
                .onFailure { exception ->
                    _forgotPasswordState.value = "Error: ${exception.localizedMessage}"
                }
        }
    }

    fun clearErrors() {
        _authUiState.value = AuthUiState.Idle
        _forgotPasswordState.value = null
    }

    fun logout() {
        userRepository.logout()
        _authUiState.value = AuthUiState.Idle
    }

    fun updateProfilePhoto(path: String) {
        viewModelScope.launch {
            userRepository.updateProfilePhoto(path)
        }
    }
}
