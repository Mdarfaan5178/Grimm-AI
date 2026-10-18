package com.example.grimm.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.grimm.model.ChatSession
import com.example.ui.theme.GrimmBorder
import com.example.ui.theme.GrimmCyan
import com.example.ui.theme.GrimmDarkBlue
import com.example.ui.theme.GrimmLightBlue
import com.example.ui.theme.GrimmLightBlueSoft
import com.example.ui.theme.GrimmOffWhite
import com.example.ui.theme.GrimmSurface
import com.example.ui.theme.GrimmTemporaryBg
import com.example.ui.theme.GrimmTemporaryBorder
import com.example.ui.theme.GrimmTemporaryPurple
import com.example.ui.theme.GrimmTextMuted
import com.example.ui.theme.GrimmTextPrimary
import com.example.ui.theme.GrimmTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GrimmChatDrawerContent(
    sessions: List<ChatSession>,
    currentSessionId: String,
    isSignedIn: Boolean,
    isGoogleSignIn: Boolean = false,
    userName: String,
    userEmail: String,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onSelectSession: (ChatSession) -> Unit,
    onNewChatClick: () -> Unit,
    onStartTemporaryChatClick: () -> Unit,
    onDeleteSession: (String) -> Unit,
    onClearAllHistory: () -> Unit,
    onAuthClick: () -> Unit,
    onOpenArtStudio: (() -> Unit)? = null,
    onCloseClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        color = GrimmOffWhite,
        modifier = modifier
            .fillMaxHeight()
            .width(320.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(16.dp)
        ) {
            // Header with title and close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Saved Chats",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrimmDarkBlue
                    )
                    Text(
                        text = if (isSignedIn) "History synced with account" else "Guest mode: Volatile sessions",
                        fontSize = 11.sp,
                        color = GrimmTextSecondary
                    )
                }
                if (onCloseClick != null) {
                    IconButton(
                        onClick = onCloseClick,
                        modifier = Modifier.testTag("drawer_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close history drawer",
                            tint = GrimmDarkBlue
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons: + New Chat and Temporary Mode button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // + New Chat button
                Button(
                    onClick = onNewChatClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GrimmLightBlue,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("new_chat_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Chat", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                // Dedicated Temporary Mode Button in the saved chat panel
                // (clears message history after the session ends)
                Button(
                    onClick = onStartTemporaryChatClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GrimmTemporaryPurple,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("drawer_temp_chat_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Temp Chat", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            // Notice about Temporary Mode
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(GrimmTemporaryBg)
                    .border(1.dp, GrimmTemporaryBorder, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = GrimmTemporaryPurple,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Temporary chats automatically clear all messages after session ends.",
                        fontSize = 11.sp,
                        color = GrimmTemporaryPurple,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Dedicated AI Art Studio Launch Banner in Drawer
            if (onOpenArtStudio != null) {
                Surface(
                    onClick = onOpenArtStudio,
                    shape = RoundedCornerShape(12.dp),
                    color = GrimmDarkBlue,
                    border = androidx.compose.foundation.BorderStroke(1.dp, GrimmCyan.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .testTag("drawer_art_studio_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(GrimmLightBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoFixHigh,
                                    contentDescription = null,
                                    tint = GrimmCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "AI Art Studio",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Synthesize visuals from prompts",
                                    fontSize = 10.sp,
                                    color = GrimmCyan
                                )
                            }
                        }
                        Text(
                            text = "Open 🪄",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GrimmCyan
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Search filter
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search conversations...", fontSize = 12.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = GrimmTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                },
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = GrimmTextPrimary,
                    fontSize = 12.sp
                ),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
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
                    .testTag("search_history_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Saved sessions list or empty notice
            if (sessions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = null,
                            tint = GrimmTextMuted,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (!isSignedIn) "No saved chats in guest mode" else "No saved conversations found",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = GrimmTextSecondary
                        )
                        Text(
                            text = if (!isSignedIn) "Toggle Sign In below to simulate saving past chats into history." else "Start a new conversation to begin.",
                            fontSize = 11.sp,
                            color = GrimmTextMuted,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(sessions, key = { it.id }) { session ->
                        val isSelected = session.id == currentSessionId
                        val dateFormatted = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
                            .format(Date(session.updatedAt))

                        Surface(
                            onClick = { onSelectSession(session) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) GrimmLightBlueSoft else GrimmSurface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) GrimmLightBlue else GrimmBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("session_item_${session.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ChatBubbleOutline,
                                        contentDescription = null,
                                        tint = if (isSelected) GrimmLightBlue else GrimmTextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = session.title,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) GrimmDarkBlue else GrimmTextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "$dateFormatted • ${session.messages.size} msgs",
                                            fontSize = 10.sp,
                                            color = GrimmTextMuted
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = { onDeleteSession(session.id) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete conversation",
                                        tint = GrimmTextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = GrimmBorder)
            Spacer(modifier = Modifier.height(10.dp))

            // User Profile and Sign In / Sign Up toggle panel
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = GrimmSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, GrimmBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isSignedIn && isGoogleSignIn) Color.White else if (isSignedIn) GrimmLightBlue else GrimmBorder),
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
                                    Text(
                                        text = if (isSignedIn) userName.take(1).uppercase() else "G",
                                        color = if (isSignedIn) Color.White else GrimmTextSecondary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (isSignedIn) userName else "Guest User",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GrimmDarkBlue
                                    )
                                    if (isSignedIn && isGoogleSignIn) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Surface(
                                            color = Color(0xFFE8F0FE),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "Google",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1A73E8),
                                                modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = if (isSignedIn) userEmail else "Chats not synced",
                                    fontSize = 11.sp,
                                    color = GrimmTextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Sign In / Sign Up button
                    Button(
                        onClick = onAuthClick,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSignedIn) GrimmDarkBlue else Color.White,
                            contentColor = if (isSignedIn) Color.White else GrimmDarkBlue
                        ),
                        border = if (!isSignedIn) BorderStroke(1.dp, Color(0xFFDADCE0)) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("drawer_auth_toggle_button")
                    ) {
                        if (!isSignedIn) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_google_logo),
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Sign in with Google",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF3C4043)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Manage Account / Sign Out",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
