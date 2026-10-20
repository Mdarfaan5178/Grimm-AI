package com.example.grimm.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.filled.ShortText
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.platform.LocalContext
import coil.compose.SubcomposeAsyncImage
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.grimm.model.AssistantMode
import com.example.grimm.model.OutputStyle
import com.example.grimm.ui.components.GrimmAuthDialog
import com.example.grimm.ui.components.GrimmChatDrawerContent
import com.example.grimm.ui.components.GrimmImageGenCard
import com.example.grimm.ui.components.GrimmImagePreviewDialog
import com.example.grimm.ui.components.GrimmMessageItem
import com.example.grimm.ui.components.GrimmSettingsDialog
import com.example.grimm.ui.components.GrimmTopBar
import androidx.compose.material3.VerticalDivider
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
import com.example.grimm.viewmodel.GrimmViewModel
import kotlinx.coroutines.launch

@Composable
fun GrimmMainScreen(
    viewModel: GrimmViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val sessions by viewModel.filteredSessions.collectAsState()
    val isSignedIn by viewModel.isSignedIn.collectAsState()
    val isGoogleSignIn by viewModel.isGoogleSignIn.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val isSpeaking by viewModel.voiceManager.isSpeaking.collectAsState()
    val context = LocalContext.current

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Scroll to bottom when new messages arrive
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    // Modal dialogs
    if (uiState.showSettingsDialog) {
        GrimmSettingsDialog(
            selectedPersona = uiState.persona,
            onPersonaChange = { viewModel.setPersona(it) },
            selectedTone = uiState.tone,
            onToneChange = { viewModel.setTone(it) },
            selectedVoiceProfile = uiState.voiceProfile,
            onVoiceProfileChange = { viewModel.setVoiceProfile(it) },
            voiceSpeed = uiState.voiceSpeed,
            onVoiceSpeedChange = { viewModel.setVoiceSpeed(it) },
            voicePitch = uiState.voicePitch,
            onVoicePitchChange = { viewModel.setVoicePitch(it) },
            autoTts = uiState.autoTts,
            onAutoTtsChange = { viewModel.setAutoTts(it) },
            onTestVoice = { viewModel.testVoice() },
            onClearAllHistory = {
                viewModel.clearAllSavedChats()
                viewModel.setShowSettingsDialog(false)
            },
            onDismiss = { viewModel.setShowSettingsDialog(false) },
            userName = userName,
            onUserNameChange = { viewModel.updateUserName(it) }
        )
    }

    if (uiState.showAuthDialog) {
        GrimmAuthDialog(
            isSignedIn = isSignedIn,
            isGoogleSignIn = isGoogleSignIn,
            currentName = userName,
            currentEmail = userEmail,
            isSigningInWithGoogle = uiState.isSigningInWithGoogle,
            googleAuthError = uiState.googleAuthError,
            onSignIn = { name, email -> viewModel.signIn(name, email) },
            onGoogleSignIn = { preferredEmail -> viewModel.signInWithGoogle(context, preferredEmail) },
            onSignOut = { viewModel.signOut() },
            onDismiss = { viewModel.setShowAuthDialog(false) }
        )
    }

    if (uiState.previewImageUrl != null) {
        GrimmImagePreviewDialog(
            imageUrl = uiState.previewImageUrl!!,
            onDismiss = { viewModel.setPreviewImageUrl(null) }
        )
    }

    // Responsive layout detection: Desktop / Tablet (> 720dp) vs Mobile
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isDesktopScreen = maxWidth >= 760.dp

        if (isDesktopScreen) {
            // DESKTOP / TABLET TWO-PANE LAYOUT
            Row(modifier = Modifier.fillMaxSize().background(GrimmOffWhite)) {
                // Persistent Sidebar for saved chat history
                GrimmChatDrawerContent(
                    sessions = sessions,
                    currentSessionId = uiState.currentSessionId,
                    isSignedIn = isSignedIn,
                    isGoogleSignIn = isGoogleSignIn,
                    userName = userName,
                    userEmail = userEmail,
                    searchQuery = uiState.searchQuery,
                    onSearchChange = { viewModel.setSearchQuery(it) },
                    onSelectSession = { viewModel.switchSession(it) },
                    onNewChatClick = { viewModel.startNewConversation(isTemporary = false) },
                    onStartTemporaryChatClick = { viewModel.startNewConversation(isTemporary = true) },
                    onDeleteSession = { viewModel.deleteSession(it) },
                    onClearAllHistory = { viewModel.clearAllSavedChats() },
                    onAuthClick = { viewModel.setShowAuthDialog(true) },
                    onOpenArtStudio = { viewModel.setImageMode(true) },
                    onCloseClick = null,
                    modifier = Modifier
                )

                VerticalDivider(color = GrimmBorder)

                // Main content column centered with max width
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    GrimmChatContent(
                        viewModel = viewModel,
                        uiState = uiState,
                        isSignedIn = isSignedIn,
                        isGoogleSignIn = isGoogleSignIn,
                        userName = userName,
                        isSpeaking = isSpeaking,
                        onOpenDrawer = { /* In desktop, sidebar is always visible */ },
                        listState = listState,
                        modifier = Modifier.widthIn(max = 880.dp)
                    )
                }
            }
        } else {
            // MOBILE SINGLE-PANE LAYOUT with Modal Navigation Drawer
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        drawerContainerColor = GrimmOffWhite
                    ) {
                        GrimmChatDrawerContent(
                            sessions = sessions,
                            currentSessionId = uiState.currentSessionId,
                            isSignedIn = isSignedIn,
                            isGoogleSignIn = isGoogleSignIn,
                            userName = userName,
                            userEmail = userEmail,
                            searchQuery = uiState.searchQuery,
                            onSearchChange = { viewModel.setSearchQuery(it) },
                            onSelectSession = {
                                viewModel.switchSession(it)
                                scope.launch { drawerState.close() }
                            },
                            onNewChatClick = {
                                viewModel.startNewConversation(isTemporary = false)
                                scope.launch { drawerState.close() }
                            },
                            onStartTemporaryChatClick = {
                                viewModel.startNewConversation(isTemporary = true)
                                scope.launch { drawerState.close() }
                            },
                            onDeleteSession = { viewModel.deleteSession(it) },
                            onClearAllHistory = {
                                viewModel.clearAllSavedChats()
                                scope.launch { drawerState.close() }
                            },
                            onAuthClick = {
                                scope.launch { drawerState.close() }
                                viewModel.setShowAuthDialog(true)
                            },
                            onOpenArtStudio = {
                                viewModel.setImageMode(true)
                                scope.launch { drawerState.close() }
                            },
                            onCloseClick = { scope.launch { drawerState.close() } }
                        )
                    }
                }
            ) {
                GrimmChatContent(
                    viewModel = viewModel,
                    uiState = uiState,
                    isSignedIn = isSignedIn,
                    isGoogleSignIn = isGoogleSignIn,
                    userName = userName,
                    isSpeaking = isSpeaking,
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    listState = listState
                )
            }
        }
    }
}

@Composable
fun GrimmChatContent(
    viewModel: GrimmViewModel,
    uiState: com.example.grimm.viewmodel.GrimmUiState,
    isSignedIn: Boolean,
    isGoogleSignIn: Boolean = false,
    userName: String,
    isSpeaking: Boolean,
    onOpenDrawer: () -> Unit,
    listState: androidx.compose.foundation.lazy.LazyListState,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            GrimmTopBar(
                currentMode = uiState.currentMode,
                isImageMode = uiState.isImageMode,
                isSignedIn = isSignedIn,
                isGoogleSignIn = isGoogleSignIn,
                userName = userName,
                onMenuClick = onOpenDrawer,
                onModeToggle = { viewModel.toggleMode() },
                onImageModeToggle = { viewModel.toggleImageMode() },
                onSettingsClick = { viewModel.setShowSettingsDialog(true) },
                onAuthClick = { viewModel.setShowAuthDialog(true) }
            )
        },
        floatingActionButton = {
            // Compact icon-only FAB for AI Art Studio generator
            FloatingActionButton(
                onClick = { viewModel.toggleImageMode() },
                containerColor = if (uiState.isImageMode) GrimmLightBlue else GrimmDarkBlue,
                contentColor = if (uiState.isImageMode) Color.White else GrimmCyan,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 6.dp
                ),
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 64.dp)
                    .size(46.dp)
                    .testTag("floating_art_studio_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.AutoFixHigh,
                    contentDescription = if (uiState.isImageMode) "Close AI Art Studio" else "Open AI Art Studio",
                    modifier = Modifier.size(22.dp)
                )
            }
        },
        contentWindowInsets = WindowInsets.statusBars,
        containerColor = GrimmOffWhite,
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Temporary Mode Warning Banner (if temporary mode is active)
            AnimatedVisibility(
                visible = uiState.currentMode == AssistantMode.TEMPORARY,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    color = GrimmTemporaryBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, GrimmTemporaryBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = GrimmTemporaryPurple,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Temporary Mode: Chats are volatile & will not be saved.",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = GrimmTemporaryPurple
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(GrimmTemporaryPurple)
                                .clickable { viewModel.setMode(AssistantMode.NORMAL) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Exit Temp",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Output Style and Controls Chips Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GrimmSurface)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Format:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GrimmTextSecondary
                )

                // Output style selectors
                OutputStyle.values().forEach { style ->
                    val isSelected = uiState.outputStyle == style
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setOutputStyle(style) },
                        label = { Text(style.label, fontSize = 11.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = when (style) {
                                    OutputStyle.PARAGRAPH -> Icons.AutoMirrored.Filled.Notes
                                    OutputStyle.BULLET_POINTS -> Icons.AutoMirrored.Filled.FormatListBulleted
                                    OutputStyle.SHORT_SENTENCES -> Icons.AutoMirrored.Filled.ShortText
                                    OutputStyle.CODE_BLOCKS -> Icons.Default.Code
                                    OutputStyle.TABLES -> Icons.Default.TableChart
                                },
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GrimmDarkBlue,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White,
                            containerColor = GrimmOffWhite,
                            labelColor = GrimmTextPrimary
                        ),
                        modifier = Modifier.testTag("style_chip_${style.name}")
                    )
                }

                // Live Web Search toggle chip
                FilterChip(
                    selected = uiState.isWebSearchActive,
                    onClick = { viewModel.toggleWebSearch() },
                    label = { Text("Web Search", fontSize = 11.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF0284C7),
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White,
                        containerColor = GrimmOffWhite,
                        labelColor = GrimmTextPrimary
                    ),
                    modifier = Modifier.testTag("toggle_web_search_chip")
                )

                // Audio Stop indicator (if speaking)
                if (isSpeaking) {
                    Surface(
                        onClick = { viewModel.stopSpeaking() },
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFEF4444)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Stop voice",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Stop Voice", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            HorizontalDivider(color = GrimmBorder)

            // Collapsible Image Generation Box
            AnimatedVisibility(visible = uiState.isImageMode) {
                GrimmImageGenCard(
                    prompt = uiState.imagePrompt,
                    onPromptChange = { viewModel.setImagePrompt(it) },
                    selectedStyle = uiState.imageStyle,
                    onStyleSelect = { viewModel.setImageStyle(it) },
                    selectedAspectRatio = uiState.aspectRatio,
                    onAspectRatioSelect = { viewModel.setAspectRatio(it) },
                    onGenerateClick = {
                        viewModel.generateImageFromPrompt(it)
                    },
                    onCloseClick = { viewModel.setImageMode(false) }
                )
            }

            // Message List Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (uiState.messages.isEmpty()) {
                    // Empty welcome illustration state
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Hero banner art
                        Image(
                            painter = painterResource(id = R.drawable.grimm),
                            contentDescription = "Grimm AI Hero Banner",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(16.dp))
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Welcome to Grimm AI",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = GrimmDarkBlue
                        )
                        Text(
                            text = "Dual-mode cognitive assistant with live image synthesis and voice control",
                            fontSize = 12.sp,
                            color = GrimmTextSecondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Suggestion prompt chips
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val suggestions = listOf(
                                "Explain Kotlin Coroutines StateFlow vs SharedFlow",
                                "Search live web: Latest breakthroughs in astronomy & space exploration",
                                "Render visual: A glowing cobalt raven in cyber library"
                            )
                            suggestions.forEach { prompt ->
                                Surface(
                                    onClick = {
                                        viewModel.setUserInput(prompt)
                                        viewModel.sendMessage()
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    color = GrimmSurface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, GrimmBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = GrimmLightBlue,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(prompt, fontSize = 12.sp, color = GrimmDarkBlue)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("messages_lazy_column")
                    ) {
                        items(uiState.messages, key = { it.id }) { message ->
                            GrimmMessageItem(
                                message = message,
                                onSpeakClick = { text -> viewModel.speakMessage(text) },
                                onImageClick = { url -> viewModel.setPreviewImageUrl(url) }
                            )
                        }

                        if (uiState.isLoading) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        color = if (uiState.isWebSearchActive) Color(0xFF0284C7) else GrimmLightBlue,
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = uiState.searchStatusMessage ?: if (uiState.isWebSearchActive) "Searching live web & synthesizing..." else "Grimm AI is analyzing...",
                                        fontSize = 12.sp,
                                        color = if (uiState.isWebSearchActive) Color(0xFF0369A1) else GrimmTextSecondary,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Chat Input Bar at Bottom
            val context = LocalContext.current
            val photoPickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.PickVisualMedia()
            ) { uri ->
                if (uri != null) {
                    viewModel.processSelectedImage(uri, context)
                }
            }

            Surface(
                color = GrimmSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, GrimmBorder),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Attached picture preview thumbnail
                    if (uiState.attachedImageUri != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = GrimmLightBlueSoft,
                            border = androidx.compose.foundation.BorderStroke(1.dp, GrimmLightBlue),
                            modifier = Modifier
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    SubcomposeAsyncImage(
                                        model = uiState.attachedImageUri,
                                        contentDescription = "Attached thumbnail",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Picture attached",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GrimmDarkBlue
                                        )
                                        Text(
                                            text = "Ask Grimm AI anything about this photo",
                                            fontSize = 11.sp,
                                            color = GrimmTextSecondary
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = { viewModel.clearAttachedImage() },
                                    modifier = Modifier
                                        .size(28.dp)
                                        .testTag("clear_attached_image_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove attached image",
                                        tint = GrimmTextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Attach Picture button (Photo Picker)
                        IconButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("attach_picture_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = "Attach picture from gallery",
                                tint = if (uiState.attachedImageUri != null) GrimmLightBlue else GrimmDarkBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Live Web Search toggle button
                        IconButton(
                            onClick = { viewModel.toggleWebSearch() },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("toggle_web_search_button")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(if (uiState.isWebSearchActive) Color(0xFFE0F2FE) else Color.Transparent),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = if (uiState.isWebSearchActive) "Live Web Search Active" else "Enable Live Web Search",
                                    tint = if (uiState.isWebSearchActive) Color(0xFF0284C7) else GrimmDarkBlue,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        OutlinedTextField(
                            value = uiState.userInput,
                            onValueChange = { viewModel.setUserInput(it) },
                            placeholder = {
                                Text(
                                    text = when {
                                        uiState.isImageMode -> "Enter visual prompt to generate..."
                                        uiState.isWebSearchActive -> "Search live web with Grimm AI..."
                                        uiState.attachedImageUri != null -> "Ask Grimm AI about this picture..."
                                        else -> "Message Grimm AI or search web..."
                                    },
                                    fontSize = 13.sp,
                                    color = GrimmTextMuted
                                )
                            },
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = GrimmTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = { viewModel.sendMessage() }),
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = GrimmTextPrimary,
                                unfocusedTextColor = GrimmTextPrimary,
                                cursorColor = GrimmLightBlue,
                                focusedBorderColor = GrimmLightBlue,
                                unfocusedBorderColor = GrimmBorder,
                                focusedContainerColor = GrimmOffWhite,
                                unfocusedContainerColor = GrimmOffWhite
                            ),
                            maxLines = 4,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_input_text_field")
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        // Send Button
                        val canSend = (uiState.userInput.isNotBlank() || uiState.attachedImageUri != null) && !uiState.isLoading
                        Surface(
                            onClick = { viewModel.sendMessage() },
                            enabled = canSend,
                            shape = CircleShape,
                            color = if (canSend) GrimmLightBlue else GrimmBorder,
                            modifier = Modifier
                                .size(42.dp)
                                .testTag("send_message_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = "Send prompt",
                                    tint = if (canSend) Color.White else GrimmTextMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
