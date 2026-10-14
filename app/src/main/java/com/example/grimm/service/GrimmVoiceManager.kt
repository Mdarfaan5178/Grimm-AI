package com.example.grimm.service

import android.content.Context
import android.media.AudioAttributes
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

enum class GrimmVoiceProfile(
    val title: String,
    val subtitle: String,
    val defaultPitch: Float,
    val defaultSpeed: Float,
    val voiceType: String
) {
    STORYTELLER(
        title = "Grimm Lorekeeper",
        subtitle = "Deep, articulate British & mystical cadence for lore & code",
        defaultPitch = 0.95f,
        defaultSpeed = 0.96f,
        voiceType = "british_deep"
    ),
    AURA_BALANCED(
        title = "Aura Modern",
        subtitle = "Crisp, natural companion voice with clear balanced inflection",
        defaultPitch = 1.0f,
        defaultSpeed = 1.02f,
        voiceType = "natural_female"
    ),
    KORE_WARMTH(
        title = "Kore Gentle",
        subtitle = "Warm, soothing, and encouraging conversational cadence",
        defaultPitch = 1.02f,
        defaultSpeed = 0.96f,
        voiceType = "warm_female"
    ),
    FENRIR_SAGE(
        title = "Fenrir Scholar",
        subtitle = "Articulate, measured, and authoritative analytical tone",
        defaultPitch = 0.96f,
        defaultSpeed = 0.98f,
        voiceType = "scholar_male"
    ),
    PUCK_CYBER(
        title = "Puck Dynamic",
        subtitle = "Energetic, bright, and quick paced interactive assistant",
        defaultPitch = 1.04f,
        defaultSpeed = 1.05f,
        voiceType = "bright_dynamic"
    )
}

class GrimmVoiceManager(context: Context) {
    private var tts: TextToSpeech? = null
    private var isReady = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _engineStatus = MutableStateFlow("Initializing Natural Audio Engine...")
    val engineStatus: StateFlow<String> = _engineStatus.asStateFlow()

    private var currentPitch: Float = 1.0f
    private var currentSpeed: Float = 1.0f
    private var currentProfile: GrimmVoiceProfile = GrimmVoiceProfile.AURA_BALANCED

    private val availableVoices = mutableListOf<Voice>()

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                try {
                    // Configure studio media audio attributes for richer, cleaner frequency response
                    val audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                    tts?.setAudioAttributes(audioAttributes)

                    val result = tts?.setLanguage(Locale.US)
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.w("GrimmVoiceManager", "Default US English missing, checking locale fallbacks")
                        tts?.setLanguage(Locale.getDefault())
                    }

                    // Cache and inspect available voices
                    tts?.voices?.let { voices ->
                        availableVoices.clear()
                        availableVoices.addAll(voices.filter { it.locale.language == "en" })
                    }

                    isReady = true
                    _engineStatus.value = "Enhanced Speech Engine Ready (${availableVoices.size} voices)"

                    // Select voice according to active profile
                    applyVoiceProfile(currentProfile)
                } catch (e: Exception) {
                    Log.w("GrimmVoiceManager", "Error tuning TTS voice attributes", e)
                    isReady = true
                    _engineStatus.value = "Speech Engine Ready (Basic)"
                }
            } else {
                Log.e("GrimmVoiceManager", "Failed to initialize TTS engine")
                _engineStatus.value = "TTS Initialization Failed"
            }
        }

        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isSpeaking.value = true
            }

            override fun onDone(utteranceId: String?) {
                _isSpeaking.value = false
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                _isSpeaking.value = false
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                _isSpeaking.value = false
            }
        })
    }

    /**
     * Finds and applies the most resonant, high-quality voice for each persona.
     */
    private fun tuneSystemVoiceForProfile(profile: GrimmVoiceProfile) {
        if (availableVoices.isEmpty() || tts == null) return

        try {
            val candidate: Voice? = when (profile.voiceType) {
                "british_deep" -> {
                    // Prefer British English or deep male voice
                    availableVoices.firstOrNull { it.locale.country.equals("GB", ignoreCase = true) }
                        ?: availableVoices.firstOrNull { it.name.contains("male", ignoreCase = true) || it.name.contains("cxx", ignoreCase = true) || it.name.contains("tpd", ignoreCase = true) }
                }
                "warm_female" -> {
                    // Gentle/warm female voice
                    availableVoices.firstOrNull { it.name.contains("female", ignoreCase = true) || it.name.contains("iol", ignoreCase = true) || it.name.contains("sfg", ignoreCase = true) }
                }
                "scholar_male" -> {
                    // Articulate male scholar
                    availableVoices.firstOrNull { it.name.contains("male", ignoreCase = true) || it.name.contains("cxx", ignoreCase = true) }
                }
                "bright_dynamic" -> {
                    // Australian or dynamic English
                    availableVoices.firstOrNull { it.locale.country.equals("AU", ignoreCase = true) }
                        ?: availableVoices.firstOrNull { it.name.contains("network", ignoreCase = true) }
                }
                else -> { // "natural_female" or default
                    availableVoices.firstOrNull { it.quality == Voice.QUALITY_VERY_HIGH }
                        ?: availableVoices.firstOrNull { it.name.contains("neural", ignoreCase = true) || it.name.contains("sfg", ignoreCase = true) }
                }
            }

            if (candidate != null) {
                tts?.voice = candidate
                Log.d("GrimmVoiceManager", "Selected voice: ${candidate.name} for ${profile.title}")
            }
        } catch (e: Exception) {
            Log.w("GrimmVoiceManager", "Voice tuning fallback", e)
        }
    }

    fun applyVoiceProfile(profile: GrimmVoiceProfile) {
        currentProfile = profile
        tuneSystemVoiceForProfile(profile)
        applyVoiceSettings(profile.defaultPitch, profile.defaultSpeed)
    }

    fun applyVoiceSettings(pitch: Float, speed: Float) {
        // Coerce into a smooth, natural range to prevent robotic clipping or distortion
        currentPitch = pitch.coerceIn(0.75f, 1.4f)
        currentSpeed = speed.coerceIn(0.75f, 1.4f)
        if (isReady) {
            tts?.setPitch(currentPitch)
            tts?.setSpeechRate(currentSpeed)
        }
    }

    fun resetToProfileDefaults() {
        applyVoiceProfile(currentProfile)
    }

    fun speak(text: String, utteranceId: String = "grimm_speech_${System.currentTimeMillis()}") {
        if (!isReady || tts == null) return
        stop()

        tuneSystemVoiceForProfile(currentProfile)
        tts?.setPitch(currentPitch)
        tts?.setSpeechRate(currentSpeed)

        val cleanSpeech = sanitizeForNaturalSpeech(text)
        if (cleanSpeech.isBlank()) return

        // Break very long passages into natural sentence segments to ensure fluid cadence
        val sentences = cleanSpeech.split(Regex("(?<=[.!?])\\s+")).filter { it.isNotBlank() }

        if (sentences.isEmpty()) {
            tts?.speak(cleanSpeech, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        } else {
            sentences.forEachIndexed { index, sentence ->
                val queueMode = if (index == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
                val sentenceUtteranceId = "${utteranceId}_part_$index"
                tts?.speak(sentence, queueMode, null, sentenceUtteranceId)
            }
        }
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun testVoice(sampleText: String? = null) {
        val testLine = sampleText ?: when (currentProfile) {
            GrimmVoiceProfile.STORYTELLER -> "Greetings seeker. I am Grimm AI, recounting fables and unraveling architecture with deep resonance."
            GrimmVoiceProfile.AURA_BALANCED -> "Hello there! I am Grimm AI, calibrated with natural inflection and clear articulation."
            GrimmVoiceProfile.KORE_WARMTH -> "Welcome. I am happy to assist you today with thoughtful answers and gentle guidance."
            GrimmVoiceProfile.FENRIR_SAGE -> "System nominal. Grimm analytical matrix active. Ready for code reviews and inquiries."
            GrimmVoiceProfile.PUCK_CYBER -> "Ready when you are! Let's craft visuals, debug logic, and build something awesome!"
        }
        speak(testLine)
    }

    /**
     * Sanitizes raw markdown, code syntax, bullet characters, URLs, and abbreviations
     * into smooth, natural, human-sounding conversational phrasing.
     */
    private fun sanitizeForNaturalSpeech(rawText: String): String {
        var text = rawText

        // Replace code blocks with courteous spoken notification instead of reading symbols
        text = text.replace(Regex("```([a-zA-Z]*)[\\s\\S]*?```")) { match ->
            val lang = match.groupValues.getOrNull(1)?.trim()
            if (!lang.isNullOrBlank()) " Here is the $lang code snippet. " else " Here is the code snippet. "
        }

        // Inline code spans: replace `someCode` with someCode
        text = text.replace(Regex("`([^`]+)`"), "$1")

        // Strip bold, italics, strikethroughs
        text = text.replace(Regex("\\*\\*(.*?)\\*\\*"), "$1")
        text = text.replace(Regex("\\*(.*?)\\*"), "$1")
        text = text.replace(Regex("~~(.*?)~~"), "$1")
        text = text.replace(Regex("__(.*?)__"), "$1")
        text = text.replace(Regex("_(.*?)_"), "$1")

        // Strip markdown headers (# Header) -> Header.
        text = text.replace(Regex("(?m)^#{1,6}\\s*(.*?)$"), "$1. ")

        // Markdown tables: replace vertical pipes with pauses
        text = text.replace("|", ", ")

        // Replace URLs with "link"
        text = text.replace(Regex("https?://\\S+"), "link")

        // Expand common tech acronyms phonetically for clear, natural speech
        text = text.replace(Regex("\\bAI\\b"), "A.I.")
        text = text.replace(Regex("\\bAPI\\b"), "A.P.I.")
        text = text.replace(Regex("\\bUI\\b"), "U.I.")
        text = text.replace(Regex("\\bUX\\b"), "user experience")
        text = text.replace(Regex("\\bURL\\b"), "U.R.L.")
        text = text.replace(Regex("\\bURLs\\b"), "U.R.L.s")
        text = text.replace(Regex("\\bJSON\\b"), "Jason")
        text = text.replace(Regex("\\bSDK\\b"), "S.D.K.")
        text = text.replace(Regex("\\be\\.g\\.", RegexOption.IGNORE_CASE), "for example")
        text = text.replace(Regex("\\bi\\.e\\.", RegexOption.IGNORE_CASE), "that is")
        text = text.replace(Regex("\\betc\\.", RegexOption.IGNORE_CASE), "etcetera")
        text = text.replace(Regex("\\bvs\\.?\\b", RegexOption.IGNORE_CASE), "versus")
        text = text.replace(Regex("\\bw/o\\b", RegexOption.IGNORE_CASE), "without")
        text = text.replace(Regex("\\bw/\\b", RegexOption.IGNORE_CASE), "with")

        // Replace bullet points with soft spoken transitions
        text = text.replace(Regex("(?m)^[•\\-*+]\\s*"), "")
        text = text.replace("•", ", ")

        // Replace numbered list markdown like "1. " with "1, " for natural pause
        text = text.replace(Regex("(?m)^(\\d+)\\.\\s*"), "$1, ")

        // Strip emoji icons so TTS does not speak literal unicode descriptors
        text = text.replace(Regex("[\\p{So}\\p{Cn}\\uD83C-\\uDBFF\\uDC00-\\uDFFF]"), "")

        // Strip horizontal dividers
        text = text.replace(Regex("[-=_]{3,}"), "")

        // Normalize multiple spaces, tabs, and line breaks
        text = text.replace(Regex("\\s+"), " ").trim()

        return text
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
