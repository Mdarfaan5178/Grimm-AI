package com.example.grimm.model

enum class MessageSender {
    USER,
    ASSISTANT,
    SYSTEM
}

enum class OutputStyle(val label: String, val promptInstruction: String) {
    PARAGRAPH("Full Paragraphs", "Format your response in well-structured, comprehensive paragraphs."),
    BULLET_POINTS("Bullet Points", "Format your response primarily using clear, concise bullet points."),
    SHORT_SENTENCES("Short Sentences", "Format your response in brief, direct, punchy sentences without fluff.")
}

enum class AssistantMode(val title: String, val subtitle: String) {
    NORMAL("Normal Mode", "Chats saved to history"),
    TEMPORARY("Temporary Mode", "Incognito • Volatile session")
}

enum class AiTone(val label: String, val description: String) {
    FRIENDLY("Friendly", "Approachable, warm, and supportive"),
    PROFESSIONAL("Professional", "Polite, objective, and analytical"),
    CYNICAL_GRIMM("Grimm Storyteller", "Mysterious, dark wit, fairy tale lore"),
    CONCISE("Concise", "Direct to the point, minimal filler"),
    ACADEMIC("Academic", "In-depth, scholarly citations and rigor"),
    CREATIVE("Creative", "Imaginative, vivid metaphors and expressive")
}

enum class AiPersona(val label: String, val role: String) {
    GENERAL("General Assistant", "Versatile helper for any question"),
    SENIOR_DEV("Senior Engineer", "Expert software architect and coder"),
    CREATIVE_WRITER("Creative Writer", "Poetic wordsmith and storyteller"),
    TUTOR("Academic Tutor", "Patient teacher explaining complex concepts"),
    GRIMM_NARRATOR("Grimm Lorekeeper", "Chronicler of deep secrets and folklore")
}

data class WebSearchResult(
    val title: String,
    val snippet: String,
    val url: String,
    val sourceName: String,
    val timestamp: String? = null
)

data class ChatMessage(
    val id: String,
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isImage: Boolean = false,
    val imageUrl: String? = null,
    val prompt: String? = null,
    val attachedImageUri: String? = null,
    val outputStyle: OutputStyle = OutputStyle.PARAGRAPH,
    val isTemporary: Boolean = false,
    val isLoading: Boolean = false,
    val isWebSearch: Boolean = false,
    val webResults: List<WebSearchResult> = emptyList()
)

data class ChatSession(
    val id: String,
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val messages: List<ChatMessage> = emptyList(),
    val isTemporary: Boolean = false,
    val persona: AiPersona = AiPersona.GENERAL,
    val tone: AiTone = AiTone.FRIENDLY
)
