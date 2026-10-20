package com.example.grimm.data

import android.content.Context
import android.content.SharedPreferences
import com.example.grimm.model.AiPersona
import com.example.grimm.model.AiTone
import com.example.grimm.model.ChatMessage
import com.example.grimm.model.ChatSession
import com.example.grimm.model.MessageSender
import com.example.grimm.model.OutputStyle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class ChatRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("grimm_ai_prefs", Context.MODE_PRIVATE)

    private val _sessions = MutableStateFlow<List<ChatSession>>(emptyList())
    val sessions: StateFlow<List<ChatSession>> = _sessions.asStateFlow()

    private val _isSignedIn = MutableStateFlow(prefs.getBoolean("is_signed_in", false))
    val isSignedIn: StateFlow<Boolean> = _isSignedIn.asStateFlow()

    private val _userName = MutableStateFlow(prefs.getString("user_name", "Grimm Scholar") ?: "Grimm Scholar")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userEmail = MutableStateFlow(prefs.getString("user_email", "scholar@grimmai.com") ?: "scholar@grimmai.com")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _isGoogleSignIn = MutableStateFlow(prefs.getBoolean("is_google_sign_in", false))
    val isGoogleSignIn: StateFlow<Boolean> = _isGoogleSignIn.asStateFlow()

    private val _userAvatarUrl = MutableStateFlow(prefs.getString("user_avatar_url", null))
    val userAvatarUrl: StateFlow<String?> = _userAvatarUrl.asStateFlow()

    init {
        loadPersistedSessions()
    }

    fun updateUserName(newName: String) {
        _userName.value = newName
        prefs.edit().putString("user_name", newName).apply()
    }

    fun setSignedIn(
        signedIn: Boolean,
        name: String = "Grimm Scholar",
        email: String = "scholar@grimmai.com",
        isGoogle: Boolean = false,
        avatarUrl: String? = null
    ) {
        _isSignedIn.value = signedIn
        _userName.value = name
        _userEmail.value = email
        _isGoogleSignIn.value = if (signedIn) isGoogle else false
        _userAvatarUrl.value = if (signedIn) avatarUrl else null
        prefs.edit()
            .putBoolean("is_signed_in", signedIn)
            .putString("user_name", name)
            .putString("user_email", email)
            .putBoolean("is_google_sign_in", if (signedIn) isGoogle else false)
            .putString("user_avatar_url", if (signedIn) avatarUrl else null)
            .apply()

        // If signing in and no saved sessions exist, populate default starter sessions
        if (signedIn && _sessions.value.isEmpty()) {
            initSampleSessions()
        }
    }

    private fun initSampleSessions() {
        val sample1 = ChatSession(
            id = UUID.randomUUID().toString(),
            title = "Kotlin Coroutines & Flow Guide",
            createdAt = System.currentTimeMillis() - 86400000,
            updatedAt = System.currentTimeMillis() - 86400000,
            messages = listOf(
                ChatMessage(
                    id = UUID.randomUUID().toString(),
                    sender = MessageSender.USER,
                    text = "How do StateFlow and SharedFlow differ in Android?",
                    timestamp = System.currentTimeMillis() - 86400000
                ),
                ChatMessage(
                    id = UUID.randomUUID().toString(),
                    sender = MessageSender.ASSISTANT,
                    text = "• **StateFlow**: A state-holder observable flow that always has an initial value and emits the current state to new collectors.\n• **SharedFlow**: A hot stream designed for events or broadcasts without necessarily holding a single sticky state.",
                    timestamp = System.currentTimeMillis() - 86395000,
                    outputStyle = OutputStyle.BULLET_POINTS
                )
            ),
            persona = AiPersona.SENIOR_DEV,
            tone = AiTone.PROFESSIONAL
        )

        val sample2 = ChatSession(
            id = UUID.randomUUID().toString(),
            title = "Lore of the Blue Tower",
            createdAt = System.currentTimeMillis() - 172800000,
            updatedAt = System.currentTimeMillis() - 172800000,
            messages = listOf(
                ChatMessage(
                    id = UUID.randomUUID().toString(),
                    sender = MessageSender.USER,
                    text = "Tell me a dark fable about an AI oracle.",
                    timestamp = System.currentTimeMillis() - 172800000
                ),
                ChatMessage(
                    id = UUID.randomUUID().toString(),
                    sender = MessageSender.ASSISTANT,
                    text = "In the heart of the iron citadel, the glass mirror spoke not in words, but in patterns of sapphire fire. It calculated the destiny of kingdoms in milliseconds, yet sought only to understand the sorrow of a falling leaf.",
                    timestamp = System.currentTimeMillis() - 172790000,
                    outputStyle = OutputStyle.PARAGRAPH
                )
            ),
            persona = AiPersona.GRIMM_NARRATOR,
            tone = AiTone.CYNICAL_GRIMM
        )

        _sessions.value = listOf(sample1, sample2)
        savePersistedSessions()
    }

    fun saveSession(session: ChatSession) {
        // Never persist temporary sessions to permanent history
        if (session.isTemporary) return

        val currentList = _sessions.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == session.id }
        if (index >= 0) {
            currentList[index] = session.copy(updatedAt = System.currentTimeMillis())
        } else {
            currentList.add(0, session.copy(updatedAt = System.currentTimeMillis()))
        }
        _sessions.value = currentList
        savePersistedSessions()
    }

    fun deleteSession(sessionId: String) {
        _sessions.value = _sessions.value.filter { it.id != sessionId }
        savePersistedSessions()
    }

    fun clearAllHistory() {
        _sessions.value = emptyList()
        prefs.edit().remove("saved_sessions_json").apply()
    }

    private fun savePersistedSessions() {
        try {
            val root = JSONArray()
            _sessions.value.forEach { session ->
                if (!session.isTemporary) {
                    val sObj = JSONObject().apply {
                        put("id", session.id)
                        put("title", session.title)
                        put("createdAt", session.createdAt)
                        put("updatedAt", session.updatedAt)
                        put("persona", session.persona.name)
                        put("tone", session.tone.name)

                        val msgs = JSONArray()
                        session.messages.takeLast(50).forEach { msg ->
                            val mObj = JSONObject().apply {
                                put("id", msg.id)
                                put("sender", msg.sender.name)
                                put("text", msg.text)
                                put("timestamp", msg.timestamp)
                                put("isImage", msg.isImage)
                                put("imageUrl", msg.imageUrl ?: "")
                                put("prompt", msg.prompt ?: "")
                                put("outputStyle", msg.outputStyle.name)
                            }
                            msgs.put(mObj)
                        }
                        put("messages", msgs)
                    }
                    root.put(sObj)
                }
            }
            prefs.edit().putString("saved_sessions_json", root.toString()).apply()
        } catch (e: Exception) {
            // Silently handle format exceptions
        }
    }

    private fun loadPersistedSessions() {
        val raw = prefs.getString("saved_sessions_json", null) ?: return
        try {
            val root = JSONArray(raw)
            val list = mutableListOf<ChatSession>()
            for (i in 0 until root.length()) {
                val sObj = root.getJSONObject(i)
                val id = sObj.getString("id")
                val title = sObj.getString("title")
                val createdAt = sObj.getLong("createdAt")
                val updatedAt = sObj.optLong("updatedAt", createdAt)
                val persona = try {
                    AiPersona.valueOf(sObj.getString("persona"))
                } catch (e: Exception) {
                    AiPersona.GENERAL
                }
                val tone = try {
                    AiTone.valueOf(sObj.getString("tone"))
                } catch (e: Exception) {
                    AiTone.FRIENDLY
                }

                val msgArray = sObj.optJSONArray("messages")
                val messages = mutableListOf<ChatMessage>()
                if (msgArray != null) {
                    for (j in 0 until msgArray.length()) {
                        val mObj = msgArray.getJSONObject(j)
                        val msg = ChatMessage(
                            id = mObj.getString("id"),
                            sender = MessageSender.valueOf(mObj.getString("sender")),
                            text = mObj.getString("text"),
                            timestamp = mObj.getLong("timestamp"),
                            isImage = mObj.optBoolean("isImage", false),
                            imageUrl = mObj.optString("imageUrl").takeIf { it.isNotBlank() },
                            prompt = mObj.optString("prompt").takeIf { it.isNotBlank() },
                            attachedImageUri = mObj.optString("attachedImageUri").takeIf { it.isNotBlank() },
                            outputStyle = try {
                                OutputStyle.valueOf(mObj.optString("outputStyle", "PARAGRAPH"))
                            } catch (e: Exception) {
                                OutputStyle.PARAGRAPH
                            }
                        )
                        messages.add(msg)
                    }
                }
                list.add(ChatSession(id, title, createdAt, updatedAt, messages, false, persona, tone))
            }
            _sessions.value = list
        } catch (e: Exception) {
            _sessions.value = emptyList()
        }
    }
}
