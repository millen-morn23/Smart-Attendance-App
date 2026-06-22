package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.TealAccent
import com.example.ui.theme.TealPrimary
import com.example.utils.LocationUtils
import com.example.utils.PdfExporter
import com.example.viewmodel.*
import java.text.SimpleDateFormat
import java.util.*

// ==========================================
// 1. SPLASH SCREEN
// ==========================================
@Composable
fun SplashScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateToStudent: () -> Unit,
    onNavigateToLecturer: () -> Unit,
    onNavigateToAdmin: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    var startAnimation by remember { mutableStateOf(false) }
    
    val scale = animateFloatAsState(
        targetValue = if (startAnimation) 1.1f else 0.8f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessLow)
    )
    val opacity = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000)
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
        kotlinx.coroutines.delay(1800)
        
        val user = currentUser
        if (user == null) {
            onNavigateToLogin()
        } else {
            when (user.role) {
                "ADMIN" -> onNavigateToAdmin()
                "LECTURER" -> onNavigateToLecturer()
                "STUDENT" -> onNavigateToStudent()
                else -> onNavigateToLogin()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .drawBehind {
                        drawCircle(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFF2DD4BF), Color(0xFF0D9488)),
                                radius = size.minDimension / 2
                            )
                        )
                    }
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AppRegistration,
                    contentDescription = "Roster Badge Logo",
                    tint = Color.White,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "SMART ATTENDANCE",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = Color(0xFF2DD4BF)
                ),
                modifier = Modifier.testTag("splash_title")
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Autonomous GPS Verification System",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFF94A3B8),
                    letterSpacing = 0.5.sp
                )
            )

            Spacer(modifier = Modifier.height(96.dp))

            CircularProgressIndicator(
                color = Color(0xFF2DD4BF),
                strokeWidth = 3.dp,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

// ==========================================
// 2. LOGIN SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onForgotPasswordClick: () -> Unit,
    onLoginSuccess: (User) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    
    val uiState by viewModel.authUiState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(currentUser) {
        currentUser?.let {
            onLoginSuccess(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Academic Portal Login", fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Icon(
                imageVector = Icons.Default.LockPerson,
                contentDescription = "Academic Security Entrance",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Access Academic Credentials",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Identify with your registered domain email.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
            )

            // Auto shortcuts to ease testing/marking!
            Text(
                text = "Quick Mock Accounts Shortcuts",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.Start)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        email = "student@university.edu"
                        password = "password123"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) {
                    Text("Student Panel")
                }
                Button(
                    onClick = {
                        email = "lecturer@university.edu"
                        password = "password123"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) {
                    Text("Lecturer Panel")
                }
                Button(
                    onClick = {
                        email = "admin@university.edu"
                        password = "password123"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) {
                    Text("Admin Panel")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Inputs
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Academic Email Address") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email Icon") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("email_input"),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Security Password") },
                leadingIcon = { Icon(Icons.Default.Key, contentDescription = "Password Lock") },
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle password visibility"
                        )
                    }
                },
                visualTransformation = if (showPassword) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("password_input"),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onForgotPasswordClick,
                    modifier = Modifier.testTag("forgot_password_btn")
                ) {
                    Text("Reset Forgotten Crypt?", color = MaterialTheme.colorScheme.secondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState is AuthUiState.Error) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text(
                        text = (uiState as AuthUiState.Error).message,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Button(
                onClick = { viewModel.login(email, password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("login_button"),
                enabled = uiState !is AuthUiState.Loading,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (uiState is AuthUiState.Loading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Secure Portal Log In", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// 3. FORGOT PASSWORD
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    viewModel: AuthViewModel,
    onBackToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    val resetMessage by viewModel.forgotPasswordState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recover Academic Security Keys") },
                navigationIcon = {
                    IconButton(onClick = onBackToLogin) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Go back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            Icon(
                imageVector = Icons.Default.MailOutline,
                contentDescription = "Mail alert symbol",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(72.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Verify Domain Recovery",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                "A secure, encrypted link containing code reset credentials will be transmitted directly.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Your Academic Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email locator") },
                modifier = Modifier.fillMaxWidth().testTag("req_reset_email_input")
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.forgotPassword(email) },
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("trigger_reset_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Transmit Link", fontWeight = FontWeight.SemiBold)
            }

            resetMessage?.let {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (it.startsWith("Error")) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.padding(top = 24.dp).fillMaxWidth()
                ) {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// ==========================================
// 4. STUDENT DASHBOARD
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDashboardScreen(
    authViewModel: AuthViewModel,
    studentViewModel: StudentViewModel,
    onNavigateToVerify: (Long) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToNotifications: () -> Unit
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val activeSessions by studentViewModel.activeSessions.collectAsState()
    val isOnline by studentViewModel.isOnline.collectAsState(initial = true)
    
    val context = LocalContext.current
    val records by studentViewModel.getStudentHistory(currentUser?.uid ?: "").collectAsState(initial = emptyList())

    val totalAttended = records.filter { it.status == "PRESENT" }.size
    val totalMissed = records.filter { it.status == "ABSENT" }.size
    val rate = if (records.isNotEmpty()) (totalAttended * 100 / records.size) else 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Circle,
                            contentDescription = "Sync signal",
                            tint = if (isOnline) Color(0xFF2DD4BF) else Color(0xFFEF4444),
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Student Terminal", fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToNotifications) {
                        Icon(Icons.Default.Notifications, contentDescription = "Alert notifications")
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "View bio profiles")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings configs")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Bio greeting
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Face,
                            contentDescription = "Default face symbol",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Welcome, ${currentUser?.name ?: "Academic Student"}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Id: ${currentUser?.registerNo ?: "CS-XXXX-000"} | Dept: ${currentUser?.department ?: "CS"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Roster stats
            Text(
                "Classroom Term Statistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("TERM RATIO", style = MaterialTheme.typography.labelSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("$rate%", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("PRESENT", style = MaterialTheme.typography.labelSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("$totalAttended lectures", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Active Class Feeds
            Text(
                "Active Geofenced Lectures Fee",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (activeSessions.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.HourglassEmpty, contentDescription = "Vacuum indicators", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "No ongoing classes registered within standard campus hours.",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                activeSessions.forEach { session ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Animation,
                                        contentDescription = "Radar pulsing beacon",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = session.courseCode,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        "GEOFENCE RANGE: ${session.radiusMeters}m",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = session.courseTitle,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Lecturer: ${session.lecturerName}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { onNavigateToVerify(session.id) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Place, contentDescription = "Scan bounds trigger")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Check-In via GPS")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Historical scroll list
            Text(
                "Your Personal Log History",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (records.isEmpty()) {
                Text(
                    "Your check-in list is currently vacant. Your status is clear.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                records.forEach { rec ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(rec.courseCode, fontWeight = FontWeight.Bold)
                                Text(
                                    SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(rec.timestamp)),
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Text(
                                    "GPS Deviation: ${String.format("%.1f", rec.distanceMeters)}m",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (rec.isSynced) Icons.Default.CloudQueue else Icons.Default.CloudOff,
                                    contentDescription = "Sync markers",
                                    tint = if (rec.isSynced) MaterialTheme.colorScheme.primary else Color(0xFFF59E0B),
                                    modifier = Modifier.size(16.dp).padding(end = 4.dp)
                                )
                                Text(
                                    text = rec.status,
                                    fontWeight = FontWeight.Bold,
                                    color = if (rec.status == "PRESENT") Color(0xFF030712) else Color(0xFFEF4444)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. LECTURER DASHBOARD
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LecturerDashboardScreen(
    authViewModel: AuthViewModel,
    lecturerViewModel: LecturerViewModel,
    onNavigateToSessionDetail: (Long) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val sessions by lecturerViewModel.getSessionsForLecturer(currentUser?.uid ?: "").collectAsState(initial = emptyList())
    val createdSessionId by lecturerViewModel.createdSessionId.collectAsState()

    var courseCode by remember { mutableStateOf("") }
    var courseTitle by remember { mutableStateOf("") }
    var radius by remember { mutableStateOf(50.0) }
    var passcode by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf(60) }

    // Coordinates of University Core Hall (Googleplex)
    var customLat by remember { mutableStateOf(37.4220) }
    var customLon by remember { mutableStateOf(-122.0841) }

    val context = LocalContext.current

    LaunchedEffect(createdSessionId) {
        createdSessionId?.let { id ->
            Toast.makeText(context, "Attendance Session Registered!", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lecturer Terminal", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Roster Bio Profiles")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "System Settings config")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Lecture Session Creation card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Configure Academic Geofence",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = courseCode,
                        onValueChange = { courseCode = it },
                        label = { Text("Course Target Code (e.g., CS-401)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("add_course_code")
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = courseTitle,
                        onValueChange = { courseTitle = it },
                        label = { Text("Module Syllabus/Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("add_course_title")
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = passcode,
                        onValueChange = { passcode = it },
                        label = { Text("Enter 4-Digit Passcode (Optional)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("add_passcode")
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Geofence slider metrics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Class Fence Radius Limits: ${radius.toInt()}m", fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = radius.toFloat(),
                        onValueChange = { radius = it.toDouble() },
                        valueRange = 10f..500f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text("Duration of check-in eligibility: ${duration} minutes", fontWeight = FontWeight.Bold)
                    Slider(
                        value = duration.toFloat(),
                        onValueChange = { duration = it.toInt() },
                        valueRange = 10f..180f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Location coordinates (modifiable parameters)
                    Text("Latitude references: $customLat", style = MaterialTheme.typography.labelSmall)
                    Text("Longitude references: $customLon", style = MaterialTheme.typography.labelSmall)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                customLat = 37.4220
                                customLon = -122.0841
                                Toast.makeText(context, "Location locked to Campus Centre Hall", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Primary Hall", fontSize = 11.sp)
                        }
                        Button(
                            onClick = {
                                customLat = 37.4250
                                customLon = -122.0890
                                Toast.makeText(context, "Location coordinates shifted to Engineering block", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Engineering", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (courseCode.isBlank() || courseTitle.isBlank()) {
                                Toast.makeText(context, "Please write Course details.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            lecturerViewModel.createAttendanceSession(
                                courseCode = courseCode,
                                courseTitle = courseTitle,
                                lecturerId = currentUser?.uid ?: "lecturer123",
                                lecturerName = currentUser?.name ?: "Prof Charles",
                                passcode = passcode,
                                latitude = customLat,
                                longitude = customLon,
                                radiusMeters = radius,
                                durationMinutes = duration
                            )
                        },
                        modifier = Modifier.fillMaxWidth().testTag("trigger_session_creation"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.AddHomeWork, contentDescription = "Trigger session logs")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Initiate Session Geofence", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Lectures sessions created
            Text(
                "Created Academic Geofence Roster Logs",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (sessions.isEmpty()) {
                Text(
                    "You have registered no active class geofence templates.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                sessions.forEach { sess ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, if (sess.status == "ACTIVE") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable { onNavigateToSessionDetail(sess.id) }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    sess.courseCode,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (sess.status == "ACTIVE") Color(0xFFE6F4EA) else Color(0xFFFCE8E6))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = sess.status,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (sess.status == "ACTIVE") Color(0xFF137333) else Color(0xFFC5221F),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(sess.courseTitle, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "Fence Passcode: ${sess.code} | Access range: ${sess.radiusMeters}m",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Tap to manage students roster",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (sess.status == "ACTIVE") {
                                    TextButton(
                                        onClick = { lecturerViewModel.updateSessionStatus(sess, "CLOSED") },
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("Close Fence", color = Color(0xFFD93025))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 6. SESSION DETAIL SCREEN & EDIT RECORDS
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailScreen(
    sessionId: Long,
    lecturerViewModel: LecturerViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val sessions by lecturerViewModel.allSessions.collectAsState()
    val session = sessions.find { it.id == sessionId }
    
    val records by lecturerViewModel.getRecordsForSession(sessionId).collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(session?.courseCode ?: "Roster details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Return")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            session?.let {
                                PdfExporter.shareSessionReport(context, it, records)
                            }
                        }
                    ) {
                        Row(modifier = Modifier.padding(end = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Share, contentDescription = "Share PDF file sheets")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Report", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            if (session == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Attendance session record details not found.")
                }
            } else {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(session.courseTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Instructor: ${session.lecturerName}", style = MaterialTheme.typography.bodyMedium)
                        Text("Code required: ${session.code}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Text("Boundary radius limits: ${session.radiusMeters}m", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    "Checked-In Student Submissions (${records.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (records.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            "No student check-in submissions compiled yet for this class.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(records) { rec ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(rec.studentName, fontWeight = FontWeight.Bold)
                                            Text(rec.registerNo, style = MaterialTheme.typography.labelSmall)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (rec.status == "PRESENT") Color(0xFFE6F4EA) else Color(0xFFFCE8E6))
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = rec.status,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (rec.status == "PRESENT") Color(0xFF137333) else Color(0xFFC5221F),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Verified coordinates radius deviation: ${String.format("%.1f", rec.distanceMeters)}m",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )

                                    // Lecturer overrides (Edit Attendance Records feature)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Modify Roster Check: ", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                            onClick = { lecturerViewModel.modifyStudentRecordStatus(rec, "PRESENT") },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE6F4EA), contentColor = Color(0xFF137333)),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.padding(end = 4.dp).height(28.dp)
                                        ) {
                                            Text("Mark Present")
                                        }
                                        Button(
                                            onClick = { lecturerViewModel.modifyStudentRecordStatus(rec, "ABSENT") },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFCE8E6), contentColor = Color(0xFFC5221F)),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Text("Mark Absent")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 7. ADMIN DASHBOARD
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    authViewModel: AuthViewModel,
    adminViewModel: AdminViewModel,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val stats by adminViewModel.adminStats.collectAsState()
    val users by adminViewModel.allUsers.collectAsState()
    val records by adminViewModel.allRecords.collectAsState()

    var newUserName by remember { mutableStateOf("") }
    var newUserEmail by remember { mutableStateOf("") }
    var newUserRole by remember { mutableStateOf("STUDENT") }
    var newUserDept by remember { mutableStateOf("Computer Science") }
    var newUserReg by remember { mutableStateOf("") }

    val context = LocalContext.current
    var activeTab by remember { mutableStateOf(0) } // 0 = Stats/Adds, 1 = Users list, 2 = Logs

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Master Control", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profile setup")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "App properties")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            // Admin Tabs
            TabRow(selectedTabIndex = activeTab) {
                Tab(selected = activeTab == 0, onClick = { activeTab = 0 }) {
                    Text("Control", Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
                }
                Tab(selected = activeTab == 1, onClick = { activeTab = 1 }) {
                    Text("Roster (${users.size})", Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
                }
                Tab(selected = activeTab == 2, onClick = { activeTab = 2 }) {
                    Text("Audit Logs (${records.size})", Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                if (activeTab == 0) {
                    // Statistics Telemetry
                    Text("System Telemetry Stats", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(modifier = Modifier.weight(1f)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("SYSTEM ROSTER", style = MaterialTheme.typography.labelSmall)
                                Text("${stats.totalUsers}", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Card(modifier = Modifier.weight(1f)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("CHECK-IN LOGS", style = MaterialTheme.typography.labelSmall)
                                Text("${stats.totalRecords}", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // User Creation form
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                "Enlist New Member Profile",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = newUserName,
                                onValueChange = { newUserName = it },
                                label = { Text("Full Name") },
                                modifier = Modifier.fillMaxWidth().testTag("adm_add_name")
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = newUserEmail,
                                onValueChange = { newUserEmail = it },
                                label = { Text("Institutional Email Address") },
                                modifier = Modifier.fillMaxWidth().testTag("adm_add_email")
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = newUserReg,
                                onValueChange = { newUserReg = it },
                                label = { Text("Reg / Staff Matric Identification No") },
                                modifier = Modifier.fillMaxWidth().testTag("adm_add_matric")
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Text("Select Assignment Role:", fontWeight = FontWeight.Bold)
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("STUDENT", "LECTURER", "ADMIN").forEach { role ->
                                    ElevatedFilterChip(
                                        selected = newUserRole == role,
                                        onClick = { newUserRole = role },
                                        label = { Text(role) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    if (newUserName.isBlank() || newUserEmail.isBlank() || newUserReg.isBlank()) {
                                        Toast.makeText(context, "Fill in all parameters.", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    adminViewModel.createNewUserByAdmin(
                                        uid = "uid_${System.currentTimeMillis()}",
                                        name = newUserName,
                                        email = newUserEmail,
                                        role = newUserRole,
                                        dept = newUserDept,
                                        regNo = newUserReg
                                    )
                                    Toast.makeText(context, "New Card Holder added!", Toast.LENGTH_SHORT).show()
                                    // reset inputs
                                    newUserName = ""
                                    newUserEmail = ""
                                    newUserReg = ""
                                },
                                modifier = Modifier.fillMaxWidth().testTag("adm_submit_btn")
                            ) {
                                Icon(Icons.Default.PersonAdd, contentDescription = "Add profile button")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Register Member", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Admin global announcements trigger
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("System Warning Alerts Distribution", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    adminViewModel.pushGlobalSystemNotice(
                                        title = "Campus Maintenance Notice",
                                        body = "Smart Attendance servers will undergo a standard offline-sync routine tonight at 23:00."
                                    )
                                    Toast.makeText(context, "Global Broadcast transmitted successfully", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Campaign, contentDescription = "Broadcast notifications log")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Broadcast Campus Notice")
                            }
                        }
                    }
                }

                if (activeTab == 1) {
                    Text("Academic Member Roster Management", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    users.forEach { usr ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(usr.name, fontWeight = FontWeight.Bold)
                                    Text("${usr.role} | Class: ${usr.registerNo}", style = MaterialTheme.typography.bodySmall)
                                    Text(usr.email, style = MaterialTheme.typography.labelSmall)
                                }
                                IconButton(onClick = { adminViewModel.removeUser(usr) }) {
                                    Icon(Icons.Default.DeleteForever, contentDescription = "Roster clear tools", tint = Color(0xFFEF4444))
                                }
                            }
                        }
                    }
                }

                if (activeTab == 2) {
                    Text("Total Audit Track Logs", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    records.forEach { rec ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("${rec.studentName} (${rec.courseCode})", fontWeight = FontWeight.Bold)
                                    Text("Device proximity: ${String.format("%.1f", rec.distanceMeters)}m", style = MaterialTheme.typography.bodySmall)
                                    Text("Status: ${rec.status} | Synced: ${rec.isSynced}", style = MaterialTheme.typography.bodySmall)
                                }
                                IconButton(onClick = { adminViewModel.purgeRecord(rec) }) {
                                    Icon(Icons.Default.DoNotDisturbOn, contentDescription = "Acknowledge absent", tint = Color(0xFFF59E0B))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 8. GPS VERIFICATION SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GpsVerificationScreen(
    sessionId: Long,
    studentViewModel: StudentViewModel,
    authViewModel: AuthViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by authViewModel.currentUser.collectAsState()
    val activeList by studentViewModel.activeSessions.collectAsState()
    val session = activeList.find { it.id == sessionId }

    val studentLat by studentViewModel.studentLatitude.collectAsState()
    val studentLon by studentViewModel.studentLongitude.collectAsState()
    val uiState by studentViewModel.studentUiState.collectAsState()

    var enteredPasscode by remember { mutableStateOf("") }
    var locationOverrideFar by remember { mutableStateOf(false) }

    // Pulsing radar animations
    val infiniteTransition = rememberInfiniteTransition()
    val pulseSize by infiniteTransition.animateFloat(
        initialValue = 100f,
        targetValue = 280f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    LaunchedEffect(uiState) {
        if (uiState is StudentUiState.Success) {
            Toast.makeText(context, "Attendance Submitted and Processed!", Toast.LENGTH_SHORT).show()
            studentViewModel.resetState()
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Geofence Verification Center") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Cancel verification")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (session == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Subject session details not active.")
                }
            } else {
                Text(
                    text = "${session.courseCode} Check-In Portal",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = session.courseTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Pulsing Radar screen
                Box(
                    modifier = Modifier
                        .size(300.dp)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Drawing radar sweeps
                        drawCircle(
                            color = TealAccent.copy(alpha = pulseAlpha),
                            radius = pulseSize,
                            style = Stroke(width = 3.dp.toPx())
                        )
                        // Static grid
                        drawCircle(
                            color = TealPrimary.copy(alpha = 0.3f),
                            radius = 200f,
                            style = Stroke(width = 1.dp.toPx())
                        )
                        drawCircle(
                            color = TealPrimary.copy(alpha = 0.15f),
                            radius = 100f,
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "Radar satellite beacon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (locationOverrideFar) "OUTSIDE BOUNDS" else "INSIDE RANGE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = if (locationOverrideFar) Color(0xFFEF4444) else Color(0xFF2DD4BF)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Override controls (for presentation / testing purposes)
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Simulate Student Distance Proximity",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Switch coordinates to test geolocating boundary limitations.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    locationOverrideFar = false
                                    // Near (Coincides exactly)
                                    studentViewModel.setStudentCoordinates(session.latitude, session.longitude)
                                    Toast.makeText(context, "Location matches lecture hall perfectly (0m distance)", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = if (!locationOverrideFar) TealPrimary else MaterialTheme.colorScheme.surface)
                            ) {
                                Text("In Range (0m)")
                            }
                            Button(
                                onClick = {
                                    locationOverrideFar = true
                                    // Far (Over 180 meters away)
                                    studentViewModel.setStudentCoordinates(session.latitude + 0.002, session.longitude - 0.002)
                                    Toast.makeText(context, "Location coordinate set to 280m distance (Geofence Breach)", Toast.LENGTH_LONG).show()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = if (locationOverrideFar) Color(0xFFEF4444) else MaterialTheme.colorScheme.surface)
                            ) {
                                Text("Out Bounds (280m)")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Inputs
                OutlinedTextField(
                    value = enteredPasscode,
                    onValueChange = { enteredPasscode = it },
                    label = { Text("4-Digit Session Passcode") },
                    leadingIcon = { Icon(Icons.Default.Pin, contentDescription = "Passcode input locator") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("verification_code_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (uiState is StudentUiState.Error) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.padding(bottom = 12.dp).fillMaxWidth()
                    ) {
                        Text(
                            text = (uiState as StudentUiState.Error).message,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Button(
                    onClick = {
                        if (enteredPasscode.isBlank()) {
                            Toast.makeText(context, "Enter passcode.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        studentViewModel.verifyLocationAndSubmit(
                            session = session,
                            studentId = currentUser?.uid ?: "student123",
                            studentName = currentUser?.name ?: "John Doe",
                            registerNo = currentUser?.registerNo ?: "CS-049",
                            enteredClassCode = enteredPasscode
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("verify_and_submit_btn"),
                    enabled = uiState !is StudentUiState.Submitting
                ) {
                    if (uiState is StudentUiState.Submitting) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text("Verify Geofence & Check-In", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================
// 9. PROFILE & CAMERA SELECTION
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by authViewModel.currentUser.collectAsState()
    var editMode by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf(currentUser?.name ?: "") }
    var deptInput by remember { mutableStateOf(currentUser?.department ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Academic Account Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Navigate back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile photo box with simulation capture
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    .clickable {
                        // camera photo simulator triggered on tap
                        val simulatedPath = "file:///android_asset/captured_photo_${System.currentTimeMillis()}.jpg"
                        authViewModel.updateProfilePhoto(simulatedPath)
                        Toast.makeText(context, "Profile Selfie Shot Captured & Synchronized!", Toast.LENGTH_SHORT).show()
                    },
                contentAlignment = Alignment.Center
            ) {
                if (currentUser?.photoUrl != null) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Captured Profile face",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = "Default uploader camera icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
            Text(
                "Tap circle to change profile photo",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
            )

            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                label = { Text("Student Name") },
                enabled = editMode,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = currentUser?.email ?: "",
                onValueChange = {},
                label = { Text("Institutional Domain Email") },
                enabled = false,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = currentUser?.registerNo ?: "",
                onValueChange = {},
                label = { Text("Account Matriculation Index Ref") },
                enabled = false,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = deptInput,
                onValueChange = { deptInput = it },
                label = { Text("Academic Division department") },
                enabled = editMode,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (editMode) {
                        // Save triggers
                        Toast.makeText(context, "Profile credentials updated locally.", Toast.LENGTH_SHORT).show()
                    }
                    editMode = !editMode
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (editMode) "Save Records Changes" else "Edit Core Directory Profiles")
            }
        }
    }
}

// ==========================================
// 10. NOTIFICATIONS SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    studentViewModel: StudentViewModel,
    authViewModel: AuthViewModel,
    onBack: () -> Unit
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val notificationList by studentViewModel.getNotifications(currentUser?.uid ?: "").collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Roster Notice Feeds") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Go back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                "Broadcast Campus Alerts Roster",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (notificationList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "No inbox notices listed yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(notificationList) { notify ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Campaign,
                                    contentDescription = "Alert siren symbol",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp).padding(top = 2.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(notify.title, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(notify.message, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 11. GENERAL SETTINGS
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    studentViewModel: StudentViewModel,
    onBack: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val context = LocalContext.current
    val isOnline by studentViewModel.isOnline.collectAsState(initial = true)
    var allowSiren by remember { mutableStateOf(true) }
    var locationSensitivityEnabled by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Application Configurations") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Exit configuration list")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(24.dp)
        ) {
            Text("Network Synchronization Engine", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Outbox Sync Mode", fontWeight = FontWeight.Bold)
                    Text(
                        text = if (isOnline) "ONLINE: Auto-upload active records" else "OFFLINE: Queue student check-ins in local Outbox cache",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Switch(
                    checked = isOnline,
                    onCheckedChange = { studentViewModel.setNetworkStatus(it) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()

            Spacer(modifier = Modifier.height(16.dp))
            Text("Permissions & Sensor sensitivity", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Allow push alerts sound", fontWeight = FontWeight.Bold)
                    Text("Simulate push vibration log sound feeds", style = MaterialTheme.typography.bodySmall)
                }
                Switch(
                    checked = allowSiren,
                    onCheckedChange = { allowSiren = it }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Accurate Location Locks (GPS)", fontWeight = FontWeight.Bold)
                    Text("Requires High Precision system telemetry", style = MaterialTheme.typography.bodySmall)
                }
                Switch(
                    checked = locationSensitivityEnabled,
                    onCheckedChange = { locationSensitivityEnabled = it }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()

            Spacer(modifier = Modifier.height(16.dp))
            Text("About Smart Attendance System", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Smart Attendance System Client v1.0", fontWeight = FontWeight.Bold)
                    Text("University Mobile Application Project", style = MaterialTheme.typography.bodySmall)
                    Text("Engineered for Kotlin, Compose, and SQLite Room operations.", style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = onLogoutClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA4335)),
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("action_logout_btn")
            ) {
                Icon(Icons.Default.Logout, contentDescription = "Exit user session button")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Secure Log Out", fontWeight = FontWeight.Bold)
            }
        }
    }
}
