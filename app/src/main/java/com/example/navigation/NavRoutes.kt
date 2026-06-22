package com.example.navigation

object NavRoutes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val FORGOT_PASSWORD = "forgot_password"
    const val STUDENT_DASHBOARD = "student_dashboard"
    const val LECTURER_DASHBOARD = "lecturer_dashboard"
    const val ADMIN_DASHBOARD = "admin_dashboard"
    
    // Dynamic Parameterized Routes
    const val SESSION_DETAIL_TEMPLATE = "session_detail/{sessionId}"
    const val GPS_VERIFICATION_TEMPLATE = "gps_verification/{sessionId}"

    fun createSessionDetailRoute(sessionId: Long): String = "session_detail/$sessionId"
    fun createGpsVerificationRoute(sessionId: Long): String = "gps_verification/$sessionId"

    const val REPORTS = "reports"
    const val HISTORY = "history"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"
    const val NOTIFICATIONS = "notifications"
}
