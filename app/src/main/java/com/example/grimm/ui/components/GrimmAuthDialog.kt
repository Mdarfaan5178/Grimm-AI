package com.example.grimm.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.GrimmBorder
import com.example.ui.theme.GrimmCyan
import com.example.ui.theme.GrimmDarkBlue
import com.example.ui.theme.GrimmLightBlue
import com.example.ui.theme.GrimmLightBlueSoft
import com.example.ui.theme.GrimmOffWhite
import com.example.ui.theme.GrimmSurface
import com.example.ui.theme.GrimmTextMuted
import com.example.ui.theme.GrimmTextPrimary
import com.example.ui.theme.GrimmTextSecondary

@Composable
fun GrimmAuthDialog(
    isSignedIn: Boolean,
    isGoogleSignIn: Boolean = false,
    currentName: String,
    currentEmail: String,
    isSigningInWithGoogle: Boolean = false,
    googleAuthError: String? = null,
    onSignIn: (name: String, email: String) -> Unit,
    onGoogleSignIn: (preferredEmail: String?) -> Unit,
    onSignOut: () -> Unit,
    onDismiss: () -> Unit
) {
    var nameInput by remember { mutableStateOf(if (isSignedIn) currentName else "") }
    var emailInput by remember { mutableStateOf(if (isSignedIn) currentEmail else "") }
    var googleEmailInput by remember { mutableStateOf("mdarfaan5178@gmail.com") }
    var showCustomCreds by remember { mutableStateOf(false) }
    var showEditGoogleEmail by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GrimmOffWhite,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(GrimmDarkBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSignedIn && isGoogleSignIn) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_google_logo),
                                contentDescription = "Google Account",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = GrimmCyan,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isSignedIn) "Account Status" else "Sign In to Grimm AI",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrimmDarkBlue
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close dialog")
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (isSignedIn) {
                    // Signed In Status Card
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = GrimmLightBlueSoft,
                        border = BorderStroke(1.dp, GrimmLightBlue),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(if (isGoogleSignIn) Color.White else GrimmLightBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isGoogleSignIn) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_google_logo),
                                        contentDescription = "Google",
                                        tint = Color.Unspecified,
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else {
                                    Text(
                                        text = currentName.take(1).uppercase(),
                                        color = Color.White,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = currentName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GrimmDarkBlue
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Active account",
                                        tint = GrimmLightBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = currentEmail,
                                    fontSize = 12.sp,
                                    color = GrimmTextSecondary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isGoogleSignIn) {
                                        Surface(
                                            color = Color(0xFFE8F0FE),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "Google Account",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1A73E8),
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text(
                                        text = "History Synced & Saved",
                                        fontSize = 11.sp,
                                        color = GrimmDarkBlue,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "You are currently signed in. Past conversations and rendered visuals are safely preserved in your sidebar.",
                        fontSize = 12.sp,
                        color = GrimmTextSecondary
                    )
                } else {
                    // Not signed in: Show Google Sign In prominently
                    Text(
                        text = "Sign in with your Google account to save chat history, synchronize conversations, and preserve generated images.",
                        fontSize = 12.sp,
                        color = GrimmTextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Google Sign-In Primary Button
                    Surface(
                        onClick = { onGoogleSignIn(googleEmailInput) },
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFDADCE0)),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("google_sign_in_button")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (isSigningInWithGoogle) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFF4285F4)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Signing in with Google...",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF3C4043)
                                )
                            } else {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_google_logo),
                                    contentDescription = "Google G Logo",
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Sign in with Google",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF3C4043)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Spacer(modifier = Modifier.height(10.dp))

                    if (googleAuthError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFEF2F2),
                            border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = googleAuthError,
                                    fontSize = 11.sp,
                                    color = Color(0xFFB91C1C)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Expander for Custom / Manual Account
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCustomCreds = !showCustomCreds }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Or enter custom display credentials",
                            fontSize = 12.sp,
                            color = GrimmTextSecondary
                        )
                        Icon(
                            imageVector = if (showCustomCreds) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = GrimmTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    AnimatedVisibility(visible = showCustomCreds) {
                        Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = { nameInput = it },
                                label = { Text("Display Name") },
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    color = GrimmTextPrimary,
                                    fontSize = 13.sp
                                ),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = GrimmTextPrimary,
                                    unfocusedTextColor = GrimmTextPrimary,
                                    cursorColor = GrimmLightBlue,
                                    focusedBorderColor = GrimmLightBlue,
                                    unfocusedBorderColor = GrimmBorder,
                                    focusedContainerColor = GrimmSurface,
                                    unfocusedContainerColor = GrimmSurface
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_name_input")
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = emailInput,
                                onValueChange = { emailInput = it },
                                label = { Text("Email Address") },
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    color = GrimmTextPrimary,
                                    fontSize = 13.sp
                                ),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = GrimmTextPrimary,
                                    unfocusedTextColor = GrimmTextPrimary,
                                    cursorColor = GrimmLightBlue,
                                    focusedBorderColor = GrimmLightBlue,
                                    unfocusedBorderColor = GrimmBorder,
                                    focusedContainerColor = GrimmSurface,
                                    unfocusedContainerColor = GrimmSurface
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_email_input")
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (isSignedIn) {
                Button(
                    onClick = onSignOut,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEF4444),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.testTag("sign_out_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Sign Out to Guest Mode", fontSize = 12.sp)
                }
            } else {
                if (showCustomCreds) {
                    Button(
                        onClick = {
                            val validName = if (nameInput.isNotBlank()) nameInput else "Grimm Scholar"
                            val validEmail = if (emailInput.isNotBlank()) emailInput else "scholar@grimmai.com"
                            onSignIn(validName, validEmail)
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GrimmLightBlue,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.testTag("sign_in_submit_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Login,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Credentials", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isSignedIn) "Close" else "Continue as Guest", color = GrimmTextSecondary)
            }
        }
    )
}
