package com.example.grimm.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.grimm.data.ChatRepository
import com.example.grimm.model.AiPersona
import com.example.grimm.model.AiTone
import com.example.grimm.model.AssistantMode
import com.example.grimm.model.ChatMessage
import com.example.grimm.model.ChatSession
import com.example.grimm.model.MessageSender
import com.example.grimm.model.OutputStyle
import com.example.grimm.service.GeminiService
import com.example.grimm.service.GrimmVoiceManager
import com.example.grimm.service.GrimmVoiceProfile
import android.content.Context
import com.example.grimm.service.GoogleAuthHelper
import com.example.grimm.service.GoogleAuthResult
import com.example.grimm.model.WebSearchResult
import com.example.grimm.service.GrimmWebSearchService
import com.example.grimm.service.ImageAspectRatio
import com.example.grimm.service.ImageGenService
import com.example.grimm.service.ImageStyle
import com.example.grimm.service.NetworkUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class GrimmUiState(
    val currentMode: AssistantMode = AssistantMode.NORMAL,
    val outputStyle: OutputStyle = OutputStyle.PARAGRAPH,
    val isImageMode: Boolean = false,
    val persona: AiPersona = AiPersona.GENERAL,
    val tone: AiTone = AiTone.FRIENDLY,
    val voiceProfile: GrimmVoiceProfile = GrimmVoiceProfile.AURA_BALANCED,
    val voiceSpeed: Float = 1.05f,
    val voicePitch: Float = 1.0f,
    val autoTts: Boolean = false,
    val currentSessionId: String = UUID.randomUUID().toString(),
    val currentSessionTitle: String = "New Conversation",
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val userInput: String = "",
    val imagePrompt: String = "",
    val imageStyle: ImageStyle = ImageStyle.GRIMM_DARK,
    val aspectRatio: ImageAspectRatio = ImageAspectRatio.SQUARE,
    val previewImageUrl: String? = null,
    val showAuthDialog: Boolean = false,
    val showSettingsDialog: Boolean = false,
    val searchQuery: String = "",
    val temporarySessionNotice: String? = null,
    val attachedImageUri: String? = null,
    val attachedImageBase64: String? = null,
    val isSigningInWithGoogle: Boolean = false,
    val googleAuthError: String? = null,
    val isWebSearchActive: Boolean = false,
    val searchStatusMessage: String? = null
)

class GrimmViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ChatRepository(application)
    private val geminiService = GeminiService()
    private val webSearchService = GrimmWebSearchService()
    val voiceManager = GrimmVoiceManager(application)

    private val _uiState = MutableStateFlow(GrimmUiState())
    val uiState: StateFlow<GrimmUiState> = _uiState.asStateFlow()

    val isSignedIn = repository.isSignedIn
    val userName = repository.userName
    val userEmail = repository.userEmail
    val isGoogleSignIn = repository.isGoogleSignIn
    val userAvatarUrl = repository.userAvatarUrl

    // Ephemeral messages cache for temporary mode
    private var temporaryMessages = mutableListOf<ChatMessage>()

    val filteredSessions: StateFlow<List<ChatSession>> = combine(
        repository.sessions,
        _uiState
    ) { sessions, state ->
        if (state.searchQuery.isBlank()) {
            sessions
        } else {
            sessions.filter {
                it.title.contains(state.searchQuery, ignoreCase = true) ||
                it.messages.any { m -> m.text.contains(state.searchQuery, ignoreCase = true) }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Start with a welcoming message
        startNewConversation(isTemporary = false)
    }

    fun toggleMode() {
        val newMode = if (_uiState.value.currentMode == AssistantMode.NORMAL) {
            AssistantMode.TEMPORARY
        } else {
            AssistantMode.NORMAL
        }
        setMode(newMode)
    }

    fun setMode(mode: AssistantMode) {
        if (_uiState.value.currentMode == mode) return

        if (mode == AssistantMode.TEMPORARY) {
            // Entering temporary mode: clear previous temp cache and start ephemeral session
            temporaryMessages.clear()
            val tempSessionId = UUID.randomUUID().toString()
            val initialTempMsg = ChatMessage(
                id = UUID.randomUUID().toString(),
                sender = MessageSender.ASSISTANT,
                text = "Temporary Mode activated. Any questions or generated images in this session will NOT be saved to your history and will be erased when you leave.",
                timestamp = System.currentTimeMillis(),
                isTemporary = true
            )
            temporaryMessages.add(initialTempMsg)
            _uiState.value = _uiState.value.copy(
                currentMode = AssistantMode.TEMPORARY,
                currentSessionId = tempSessionId,
                currentSessionTitle = "Temporary Session",
                messages = temporaryMessages.toList(),
                temporarySessionNotice = "Temporary Mode is active. Chat history will not be saved."
            )
        } else {
            // Exiting temporary mode: clear ephemeral session data completely!
            temporaryMessages.clear()
            _uiState.value = _uiState.value.copy(
                currentMode = AssistantMode.NORMAL,
                temporarySessionNotice = null
            )
            // Restore last saved session or start new one
            val lastSaved = repository.sessions.value.firstOrNull()
            if (lastSaved != null && repository.isSignedIn.value) {
                switchSession(lastSaved)
            } else {
                startNewConversation(isTemporary = false)
            }
        }
    }

    fun setOutputStyle(style: OutputStyle) {
        _uiState.value = _uiState.value.copy(outputStyle = style)
    }

    fun toggleImageMode() {
        _uiState.value = _uiState.value.copy(isImageMode = !_uiState.value.isImageMode)
    }

    fun toggleWebSearch() {
        _uiState.value = _uiState.value.copy(isWebSearchActive = !_uiState.value.isWebSearchActive)
    }

    fun setWebSearch(active: Boolean) {
        _uiState.value = _uiState.value.copy(isWebSearchActive = active)
    }

    private fun isExplicitWebSearchIntent(query: String): Boolean {
        val q = query.trim().lowercase()
        return q.contains("browse") ||
               q.contains("search") ||
               q.contains("reaper") ||
               q.contains("google") ||
               q.contains("web") ||
               q.contains("internet") ||
               q.contains("online") ||
               q.contains("look up") ||
               q.contains("find out") ||
               q.contains("news") ||
               q.contains("latest") ||
               q.contains("today") ||
               q.contains("yesterday") ||
               q.contains("weather") ||
               q.contains("price") ||
               q.contains("score") ||
               q.contains("current") ||
               q.contains("who won") ||
               q.contains("who is") ||
               q.contains("2ho is") ||
               q.contains("release date") ||
               q.contains("http://") ||
               q.contains("https://")
    }

    fun setImageMode(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isImageMode = enabled)
    }

    fun setPersona(persona: AiPersona) {
        _uiState.value = _uiState.value.copy(persona = persona)
    }

    fun setTone(tone: AiTone) {
        _uiState.value = _uiState.value.copy(tone = tone)
    }

    fun setVoiceSpeed(speed: Float) {
        _uiState.value = _uiState.value.copy(voiceSpeed = speed)
        voiceManager.applyVoiceSettings(_uiState.value.voicePitch, speed)
    }

    fun setVoicePitch(pitch: Float) {
        _uiState.value = _uiState.value.copy(voicePitch = pitch)
        voiceManager.applyVoiceSettings(pitch, _uiState.value.voiceSpeed)
    }

    fun setVoiceProfile(profile: GrimmVoiceProfile) {
        _uiState.value = _uiState.value.copy(
            voiceProfile = profile,
            voicePitch = profile.defaultPitch,
            voiceSpeed = profile.defaultSpeed
        )
        voiceManager.applyVoiceProfile(profile)
    }

    fun setAutoTts(auto: Boolean) {
        _uiState.value = _uiState.value.copy(autoTts = auto)
    }

    fun setUserInput(input: String) {
        _uiState.value = _uiState.value.copy(userInput = input)
    }

    fun updateUserName(newName: String) {
        repository.updateUserName(newName)
    }

    fun setImagePrompt(prompt: String) {
        _uiState.value = _uiState.value.copy(imagePrompt = prompt)
    }

    fun setImageStyle(style: ImageStyle) {
        _uiState.value = _uiState.value.copy(imageStyle = style)
    }

    fun setAspectRatio(ratio: ImageAspectRatio) {
        _uiState.value = _uiState.value.copy(aspectRatio = ratio)
    }

    fun setPreviewImageUrl(url: String?) {
        _uiState.value = _uiState.value.copy(previewImageUrl = url)
    }

    fun setShowAuthDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showAuthDialog = show)
    }

    fun setShowSettingsDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showSettingsDialog = show)
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun startNewConversation(isTemporary: Boolean = false) {
        if (isTemporary) {
            setMode(AssistantMode.TEMPORARY)
            return
        }

        val newSessionId = UUID.randomUUID().toString()
        val welcomeMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            sender = MessageSender.ASSISTANT,
            text = "Greetings. I am Grimm AI, your intelligent companion. How may I assist you today? You can switch personas, adjust output style, or generate live visuals.",
            timestamp = System.currentTimeMillis()
        )
        _uiState.value = _uiState.value.copy(
            currentMode = AssistantMode.NORMAL,
            currentSessionId = newSessionId,
            currentSessionTitle = "New Conversation",
            messages = listOf(welcomeMsg),
            temporarySessionNotice = null
        )
    }

    fun switchSession(session: ChatSession) {
        // If coming from temporary mode, clear temp cache
        if (_uiState.value.currentMode == AssistantMode.TEMPORARY) {
            temporaryMessages.clear()
        }
        _uiState.value = _uiState.value.copy(
            currentMode = AssistantMode.NORMAL,
            currentSessionId = session.id,
            currentSessionTitle = session.title,
            messages = session.messages,
            persona = session.persona,
            tone = session.tone,
            temporarySessionNotice = null
        )
    }

    fun deleteSession(sessionId: String) {
        repository.deleteSession(sessionId)
        if (_uiState.value.currentSessionId == sessionId) {
            startNewConversation(isTemporary = false)
        }
    }

    fun clearAllSavedChats() {
        repository.clearAllHistory()
        startNewConversation(isTemporary = false)
    }

    fun toggleAuth() {
        val current = repository.isSignedIn.value
        repository.setSignedIn(!current)
    }

    fun signIn(name: String, email: String) {
        repository.setSignedIn(true, name, email, isGoogle = false)
        _uiState.value = _uiState.value.copy(showAuthDialog = false, googleAuthError = null)
        // Also persist current normal chat session
        persistCurrentSession()
    }

    fun signInWithGoogle(context: Context, preferredEmail: String? = null, preferredName: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSigningInWithGoogle = true, googleAuthError = null)
            // Add a brief subtle delay for a realistic native sign-in transition
            kotlinx.coroutines.delay(250)
            val webClientId = "760858662628-uqsfdq7rja3mbjfea16trtuclb5d0dnp.apps.googleusercontent.com"
            when (val result = GoogleAuthHelper.signInWithGoogle(context, webClientId = webClientId, preferredEmail = preferredEmail, preferredName = preferredName)) {
                is GoogleAuthResult.Success -> {
                    repository.setSignedIn(
                        signedIn = true,
                        name = result.displayName,
                        email = result.email,
                        isGoogle = true,
                        avatarUrl = result.avatarUrl
                    )
                    _uiState.value = _uiState.value.copy(
                        isSigningInWithGoogle = false,
                        showAuthDialog = false,
                        googleAuthError = null
                    )
                    persistCurrentSession()
                }
                is GoogleAuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isSigningInWithGoogle = false,
                        googleAuthError = if (result.isCancelled) null else result.message
                    )
                }
            }
        }
    }

    fun signInWithGoogleAccount(email: String, displayName: String? = null) {
        val cleanEmail = if (email.isNotBlank()) email.trim() else "user@gmail.com"
        val derivedName = displayName?.takeIf { it.isNotBlank() }
            ?: cleanEmail.substringBefore("@").replace(".", " ").replaceFirstChar { it.uppercase() }

        repository.setSignedIn(
            signedIn = true,
            name = derivedName,
            email = cleanEmail,
            isGoogle = true,
            avatarUrl = null
        )
        _uiState.value = _uiState.value.copy(
            isSigningInWithGoogle = false,
            showAuthDialog = false,
            googleAuthError = null
        )
        persistCurrentSession()
    }

    fun signOut() {
        repository.setSignedIn(false)
        _uiState.value = _uiState.value.copy(showAuthDialog = false, googleAuthError = null)
    }

    fun clearAttachedImage() {
        _uiState.value = _uiState.value.copy(
            attachedImageUri = null,
            attachedImageBase64 = null
        )
    }

    fun processSelectedImage(uri: android.net.Uri, context: android.content.Context) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (bitmap != null) {
                    val maxDim = 1024
                    val width = bitmap.width
                    val height = bitmap.height
                    val scaledBitmap = if (width > maxDim || height > maxDim) {
                        val ratio = width.toFloat() / height.toFloat()
                        val newWidth = if (ratio > 1) maxDim else (maxDim * ratio).toInt()
                        val newHeight = if (ratio > 1) (maxDim / ratio).toInt() else maxDim
                        android.graphics.Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
                    } else {
                        bitmap
                    }
                    val outputStream = java.io.ByteArrayOutputStream()
                    scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 85, outputStream)
                    val bytes = outputStream.toByteArray()
                    val base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                    _uiState.value = _uiState.value.copy(
                        attachedImageUri = uri.toString(),
                        attachedImageBase64 = base64
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("GrimmViewModel", "Error processing selected image", e)
            }
        }
    }

    fun sendMessage() {
        val text = _uiState.value.userInput.trim()
        val attachedUri = _uiState.value.attachedImageUri
        val attachedBase64 = _uiState.value.attachedImageBase64

        if (text.isEmpty() && attachedUri == null) return

        if (_uiState.value.isImageMode) {
            generateImageFromPrompt(text)
            _uiState.value = _uiState.value.copy(userInput = "")
            return
        }

        val isSearching = _uiState.value.isWebSearchActive || isExplicitWebSearchIntent(text)
        val cleanSearchQuery = GrimmWebSearchService.extractSearchTerms(text).ifBlank { text }

        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            sender = MessageSender.USER,
            text = if (text.isNotBlank()) text else "Analyze this image",
            attachedImageUri = attachedUri,
            timestamp = System.currentTimeMillis(),
            isTemporary = _uiState.value.currentMode == AssistantMode.TEMPORARY,
            isWebSearch = isSearching
        )

        // Offline check: immediately respond with custom network message without copy-pasting canned fallback
        if (!NetworkUtils.isNetworkAvailable(getApplication())) {
            val assistantMsg = ChatMessage(
                id = UUID.randomUUID().toString(),
                sender = MessageSender.ASSISTANT,
                text = NetworkUtils.NO_NETWORK_MESSAGE,
                timestamp = System.currentTimeMillis() + 1,
                outputStyle = _uiState.value.outputStyle,
                isTemporary = _uiState.value.currentMode == AssistantMode.TEMPORARY
            )
            val finalMessages = _uiState.value.messages + userMessage + assistantMsg
            _uiState.value = _uiState.value.copy(
                messages = finalMessages,
                userInput = "",
                attachedImageUri = null,
                attachedImageBase64 = null,
                isLoading = false,
                searchStatusMessage = null
            )
            if (_uiState.value.currentMode == AssistantMode.NORMAL) {
                persistCurrentSession()
            }
            if (_uiState.value.autoTts) {
                voiceManager.speak(NetworkUtils.NO_NETWORK_MESSAGE)
            }
            return
        }

        val updatedMessages = _uiState.value.messages + userMessage
        _uiState.value = _uiState.value.copy(
            messages = updatedMessages,
            userInput = "",
            attachedImageUri = null,
            attachedImageBase64 = null,
            isLoading = true,
            searchStatusMessage = if (isSearching) "Browsing live web for '$cleanSearchQuery'..." else null
        )

        // Title generation for first user message
        if (_uiState.value.currentSessionTitle == "New Conversation" && _uiState.value.currentMode == AssistantMode.NORMAL) {
            val titleCandidate = if (text.length > 28) text.take(28) + "..." else if (text.isNotBlank()) text else "Image Query"
            _uiState.value = _uiState.value.copy(currentSessionTitle = titleCandidate)
        }

        viewModelScope.launch {
            var webResults = emptyList<WebSearchResult>()
            if (isSearching && text.isNotBlank()) {
                try {
                    webResults = webSearchService.searchWeb(cleanSearchQuery)
                } catch (e: Exception) {
                    android.util.Log.e("GrimmViewModel", "Web search failed", e)
                    if (NetworkUtils.isNetworkException(e) || !NetworkUtils.isNetworkAvailable(getApplication())) {
                        val assistantMsg = ChatMessage(
                            id = UUID.randomUUID().toString(),
                            sender = MessageSender.ASSISTANT,
                            text = NetworkUtils.NO_NETWORK_MESSAGE,
                            timestamp = System.currentTimeMillis(),
                            outputStyle = _uiState.value.outputStyle,
                            isTemporary = _uiState.value.currentMode == AssistantMode.TEMPORARY
                        )
                        val finalMessages = _uiState.value.messages + assistantMsg
                        _uiState.value = _uiState.value.copy(
                            messages = finalMessages,
                            isLoading = false,
                            searchStatusMessage = null
                        )
                        if (_uiState.value.currentMode == AssistantMode.NORMAL) {
                            persistCurrentSession()
                        }
                        if (_uiState.value.autoTts) {
                            voiceManager.speak(NetworkUtils.NO_NETWORK_MESSAGE)
                        }
                        return@launch
                    }
                }
            }

            val historyPairs = updatedMessages
                .filter { it.sender != MessageSender.SYSTEM && !it.isImage }
                .chunked(2)
                .mapNotNull {
                    val u = it.getOrNull(0)?.text
                    val a = it.getOrNull(1)?.text
                    if (u != null && a != null) Pair(u, a) else null
                }

            val responseText = try {
                geminiService.generateResponse(
                    prompt = if (text.isNotBlank()) text else "Analyze this image",
                    persona = _uiState.value.persona,
                    tone = _uiState.value.tone,
                    style = _uiState.value.outputStyle,
                    imageBase64 = attachedBase64,
                    conversationHistory = historyPairs,
                    searchContext = if (webResults.isNotEmpty()) webResults else null,
                    context = getApplication()
                )
            } catch (e: Exception) {
                if (NetworkUtils.isNetworkException(e) || !NetworkUtils.isNetworkAvailable(getApplication())) {
                    NetworkUtils.NO_NETWORK_MESSAGE
                } else {
                    "Error generating response: ${e.message ?: "Unknown"}"
                }
            }

            val assistantMsg = ChatMessage(
                id = UUID.randomUUID().toString(),
                sender = MessageSender.ASSISTANT,
                text = responseText,
                timestamp = System.currentTimeMillis(),
                outputStyle = _uiState.value.outputStyle,
                isTemporary = _uiState.value.currentMode == AssistantMode.TEMPORARY,
                isWebSearch = webResults.isNotEmpty(),
                webResults = webResults
            )

            val finalMessages = _uiState.value.messages + assistantMsg
            _uiState.value = _uiState.value.copy(
                messages = finalMessages,
                isLoading = false,
                searchStatusMessage = null
            )

            if (_uiState.value.currentMode == AssistantMode.NORMAL) {
                persistCurrentSession()
            }

            // Speak if auto TTS is active
            if (_uiState.value.autoTts) {
                voiceManager.speak(responseText)
            }
        }
    }

    fun generateImageFromPrompt(prompt: String) {
        val cleanPrompt = prompt.trim()
        if (cleanPrompt.isEmpty()) return

        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            sender = MessageSender.USER,
            text = "Generate image: $cleanPrompt",
            timestamp = System.currentTimeMillis(),
            isTemporary = _uiState.value.currentMode == AssistantMode.TEMPORARY
        )

        // Offline check for image synthesis
        if (!NetworkUtils.isNetworkAvailable(getApplication())) {
            val assistantMsg = ChatMessage(
                id = UUID.randomUUID().toString(),
                sender = MessageSender.ASSISTANT,
                text = NetworkUtils.NO_NETWORK_MESSAGE,
                timestamp = System.currentTimeMillis() + 1,
                isTemporary = _uiState.value.currentMode == AssistantMode.TEMPORARY
            )
            val updated = _uiState.value.messages + userMessage + assistantMsg
            _uiState.value = _uiState.value.copy(
                messages = updated,
                imagePrompt = "",
                isLoading = false
            )
            if (_uiState.value.currentMode == AssistantMode.NORMAL) {
                persistCurrentSession()
            }
            if (_uiState.value.autoTts) {
                voiceManager.speak(NetworkUtils.NO_NETWORK_MESSAGE)
            }
            return
        }

        val imageUrl = ImageGenService.constructImageUrl(
            userPrompt = cleanPrompt,
            style = _uiState.value.imageStyle,
            aspectRatio = _uiState.value.aspectRatio
        )

        val imageMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            sender = MessageSender.ASSISTANT,
            text = "Rendered visual for '$cleanPrompt' [${_uiState.value.imageStyle.label}]",
            isImage = true,
            imageUrl = imageUrl,
            prompt = cleanPrompt,
            timestamp = System.currentTimeMillis(),
            isTemporary = _uiState.value.currentMode == AssistantMode.TEMPORARY
        )

        val updated = _uiState.value.messages + userMessage + imageMessage
        _uiState.value = _uiState.value.copy(
            messages = updated,
            imagePrompt = ""
        )

        if (_uiState.value.currentMode == AssistantMode.NORMAL) {
            persistCurrentSession()
        }
    }

    fun speakMessage(text: String) {
        voiceManager.speak(text)
    }

    fun stopSpeaking() {
        voiceManager.stop()
    }

    fun testVoice() {
        voiceManager.testVoice("Greetings traveler. I am Grimm AI, voice synthesis calibrated.")
    }

    private fun persistCurrentSession() {
        // Don't save if in temporary mode
        if (_uiState.value.currentMode == AssistantMode.TEMPORARY) return

        val session = ChatSession(
            id = _uiState.value.currentSessionId,
            title = _uiState.value.currentSessionTitle,
            messages = _uiState.value.messages,
            isTemporary = false,
            persona = _uiState.value.persona,
            tone = _uiState.value.tone
        )
        repository.saveSession(session)
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.shutdown()
    }
}
