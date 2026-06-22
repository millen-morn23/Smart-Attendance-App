package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.repository.AttendanceRepository
import com.example.data.repository.UserRepository
import com.example.database.AppDatabase
import com.example.navigation.NavRoutes
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AdminViewModel
import com.example.viewmodel.AuthViewModel
import com.example.viewmodel.LecturerViewModel
import com.example.viewmodel.StudentViewModel
import com.example.viewmodel.ViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Let the system draw behind the camera notch and bottom task bars
        enableEdgeToEdge()

        // 1. Core SQLite Room Database initial seeding
        val db = AppDatabase.getDatabase(applicationContext, lifecycleScope)

        // 2. Repositories initialization
        val userRepository = UserRepository(db.userDao(), applicationContext)
        val attendanceRepository = AttendanceRepository(db.attendanceDao(), db.syncQueueDao())

        // 3. Centralized ViewModels injection Setup
        val factory = ViewModelFactory(userRepository, attendanceRepository)
        val authViewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]
        val studentViewModel = ViewModelProvider(this, factory)[StudentViewModel::class.java]
        val lecturerViewModel = ViewModelProvider(this, factory)[LecturerViewModel::class.java]
        val adminViewModel = ViewModelProvider(this, factory)[AdminViewModel::class.java]

        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = NavRoutes.SPLASH,
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Gateway onboarding
                    composable(NavRoutes.SPLASH) {
                        SplashScreen(
                            viewModel = authViewModel,
                            onNavigateToLogin = {
                                navController.navigate(NavRoutes.LOGIN) {
                                    popUpTo(NavRoutes.SPLASH) { inclusive = true }
                                }
                            },
                            onNavigateToStudent = {
                                navController.navigate(NavRoutes.STUDENT_DASHBOARD) {
                                    popUpTo(NavRoutes.SPLASH) { inclusive = true }
                                }
                            },
                            onNavigateToLecturer = {
                                navController.navigate(NavRoutes.LECTURER_DASHBOARD) {
                                    popUpTo(NavRoutes.SPLASH) { inclusive = true }
                                }
                            },
                            onNavigateToAdmin = {
                                navController.navigate(NavRoutes.ADMIN_DASHBOARD) {
                                    popUpTo(NavRoutes.SPLASH) { inclusive = true }
                                }
                            }
                        )
                    }

                    // Secure logins
                    composable(NavRoutes.LOGIN) {
                        LoginScreen(
                            viewModel = authViewModel,
                            onForgotPasswordClick = {
                                navController.navigate(NavRoutes.FORGOT_PASSWORD)
                            },
                            onLoginSuccess = { user ->
                                val targetScreen = when (user.role) {
                                    "ADMIN" -> NavRoutes.ADMIN_DASHBOARD
                                    "LECTURER" -> NavRoutes.LECTURER_DASHBOARD
                                    "STUDENT" -> NavRoutes.STUDENT_DASHBOARD
                                    else -> NavRoutes.LOGIN
                                }
                                navController.navigate(targetScreen) {
                                    popUpTo(NavRoutes.LOGIN) { inclusive = true }
                                }
                            }
                        )
                    }

                    // Key recoveries
                    composable(NavRoutes.FORGOT_PASSWORD) {
                        ForgotPasswordScreen(
                            viewModel = authViewModel,
                            onBackToLogin = { navController.popBackStack() }
                        )
                    }

                    // Dynamic role layouts
                    composable(NavRoutes.STUDENT_DASHBOARD) {
                        StudentDashboardScreen(
                            authViewModel = authViewModel,
                            studentViewModel = studentViewModel,
                            onNavigateToVerify = { sessionId ->
                                navController.navigate(NavRoutes.createGpsVerificationRoute(sessionId))
                            },
                            onNavigateToProfile = { navController.navigate(NavRoutes.PROFILE) },
                            onNavigateToSettings = { navController.navigate(NavRoutes.SETTINGS) },
                            onNavigateToNotifications = { navController.navigate(NavRoutes.NOTIFICATIONS) },
                            onLogoutClick = {
                                authViewModel.logout()
                                navController.navigate(NavRoutes.LOGIN) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(NavRoutes.LECTURER_DASHBOARD) {
                        LecturerDashboardScreen(
                            authViewModel = authViewModel,
                            lecturerViewModel = lecturerViewModel,
                            onNavigateToSessionDetail = { sessionId ->
                                navController.navigate(NavRoutes.createSessionDetailRoute(sessionId))
                            },
                            onNavigateToProfile = { navController.navigate(NavRoutes.PROFILE) },
                            onNavigateToSettings = { navController.navigate(NavRoutes.SETTINGS) }
                        )
                    }

                    composable(NavRoutes.ADMIN_DASHBOARD) {
                        AdminDashboardScreen(
                            authViewModel = authViewModel,
                            adminViewModel = adminViewModel,
                            onNavigateToProfile = { navController.navigate(NavRoutes.PROFILE) },
                            onNavigateToSettings = { navController.navigate(NavRoutes.SETTINGS) }
                        )
                    }

                    // Dynamic checking screens
                    composable(
                        route = NavRoutes.SESSION_DETAIL_TEMPLATE,
                        arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: 0L
                        SessionDetailScreen(
                            sessionId = sessionId,
                            lecturerViewModel = lecturerViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = NavRoutes.GPS_VERIFICATION_TEMPLATE,
                        arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: 0L
                        GpsVerificationScreen(
                            sessionId = sessionId,
                            studentViewModel = studentViewModel,
                            authViewModel = authViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    // Utilities
                    composable(NavRoutes.PROFILE) {
                        ProfileScreen(
                            authViewModel = authViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(NavRoutes.NOTIFICATIONS) {
                        NotificationsScreen(
                            studentViewModel = studentViewModel,
                            authViewModel = authViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(NavRoutes.SETTINGS) {
                        SettingsScreen(
                            studentViewModel = studentViewModel,
                            onBack = { navController.popBackStack() },
                            onLogoutClick = {
                                authViewModel.logout()
                                navController.navigate(NavRoutes.LOGIN) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
