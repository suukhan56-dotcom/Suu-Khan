package com.example.mtaatok.ui.auth

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mtaatok.data.local.SessionManager
import com.example.mtaatok.model.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

enum class AuthMode {
    LOGIN, SIGNUP, FORGOT_PASSWORD
}

@Composable
fun AuthScreen(
    sessionManager: SessionManager,
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var authMode by remember { mutableStateOf(AuthMode.LOGIN) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF140D20),
                        Color(0xFF09090D),
                        Color.Black
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .testTag("auth_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Original MtaaTok Branding Emblem
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF00E5FF),
                                Color(0xFFFF5722),
                                Color(0xFFFF2A6D)
                            )
                        )
                    )
                    .padding(3.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(19.dp))
                        .background(Color(0xFF12121A)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "M",
                        color = Color(0xFFFF5722),
                        fontWeight = FontWeight.Black,
                        fontSize = 42.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "MtaaTok",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )

            Text(
                text = "The Pulse of the Streets • Short Videos 🇰🇪",
                color = Color.LightGray,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Auth Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF181824)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Segment Tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF101018))
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (authMode == AuthMode.LOGIN) Color(0xFFFF5722) else Color.Transparent)
                                .clickable {
                                    authMode = AuthMode.LOGIN
                                    errorMessage = null
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Log In",
                                color = Color.White,
                                fontWeight = if (authMode == AuthMode.LOGIN) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (authMode == AuthMode.SIGNUP) Color(0xFFFF5722) else Color.Transparent)
                                .clickable {
                                    authMode = AuthMode.SIGNUP
                                    errorMessage = null
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Sign Up",
                                color = Color.White,
                                fontWeight = if (authMode == AuthMode.SIGNUP) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            color = Color(0xFFFF2A6D),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    // Signup Fields
                    AnimatedVisibility(visible = authMode == AuthMode.SIGNUP) {
                        Column {
                            OutlinedTextField(
                                value = username,
                                onValueChange = { username = it.lowercase().filter { c -> c.isLetterOrDigit() || c == '_' } },
                                label = { Text("Username (e.g. nairobidancer)") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFFF5722),
                                    unfocusedBorderColor = Color(0xFF2C2C3E),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("signup_username_input")
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = displayName,
                                onValueChange = { displayName = it },
                                label = { Text("Display Name (e.g. Kelvin Nairobi)") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFFF5722),
                                    unfocusedBorderColor = Color(0xFF2C2C3E),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("signup_displayname_input")
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }

                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color.Gray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFF5722),
                            unfocusedBorderColor = Color(0xFF2C2C3E),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_email_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Password Field
                    if (authMode != AuthMode.FORGOT_PASSWORD) {
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle password",
                                        tint = Color.Gray
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFF5722),
                                unfocusedBorderColor = Color(0xFF2C2C3E),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_password_input")
                        )
                    }

                    if (authMode == AuthMode.LOGIN) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = { authMode = AuthMode.FORGOT_PASSWORD }
                            ) {
                                Text("Forgot password?", color = Color(0xFF00E5FF), fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action Button
                    Button(
                        onClick = {
                            if (authMode == AuthMode.FORGOT_PASSWORD) {
                                if (email.isBlank()) {
                                    errorMessage = "Please enter your email to reset password"
                                    return@Button
                                }
                                isLoading = true
                                coroutineScope.launch {
                                    delay(1000)
                                    isLoading = false
                                    Toast.makeText(context, "Password reset link sent to $email", Toast.LENGTH_LONG).show()
                                    authMode = AuthMode.LOGIN
                                }
                                return@Button
                            }

                            if (email.isBlank() || password.isBlank()) {
                                errorMessage = "Email and password cannot be empty"
                                return@Button
                            }

                            if (authMode == AuthMode.SIGNUP && (username.isBlank() || displayName.isBlank())) {
                                errorMessage = "Please enter username and display name"
                                return@Button
                            }

                            isLoading = true
                            coroutineScope.launch {
                                delay(800) // smooth session auth simulation
                                isLoading = false

                                val finalUsername = if (authMode == AuthMode.SIGNUP) username.trim() else email.substringBefore("@")
                                val finalDisplayName = if (authMode == AuthMode.SIGNUP) displayName.trim() else finalUsername.replaceFirstChar { it.uppercase() }

                                val user = User(
                                    id = "usr_${UUID.randomUUID().toString().take(8)}",
                                    username = finalUsername,
                                    displayName = finalDisplayName,
                                    email = email.trim(),
                                    bio = "MtaaTok Creator • Street Stories & Energy 🇰🇪",
                                    avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200",
                                    followersCount = 120,
                                    followingCount = 45,
                                    totalLikesCount = 380,
                                    isVerified = true,
                                    isCreatorEligible = true
                                )

                                sessionManager.saveUserSession(user)
                                onAuthSuccess()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("auth_action_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722)),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                        } else {
                            Text(
                                text = when (authMode) {
                                    AuthMode.LOGIN -> "Log In to MtaaTok"
                                    AuthMode.SIGNUP -> "Create Account"
                                    AuthMode.FORGOT_PASSWORD -> "Send Reset Link"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }

                    if (authMode == AuthMode.FORGOT_PASSWORD) {
                        Spacer(modifier = Modifier.height(10.dp))
                        TextButton(onClick = { authMode = AuthMode.LOGIN }) {
                            Text("Back to Log In", color = Color.Gray, fontSize = 13.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "By continuing, you agree to MtaaTok Community Guidelines and Terms.",
                color = Color.Gray,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
