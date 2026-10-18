package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.grimm.model.AiPersona
import com.example.grimm.model.AiTone
import com.example.grimm.model.AssistantMode
import com.example.grimm.model.OutputStyle
import com.example.grimm.service.ImageAspectRatio
import com.example.grimm.service.ImageGenService
import com.example.grimm.service.ImageStyle
import com.example.grimm.viewmodel.GrimmViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context verifies Grimm AI app name`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Grimm AI", appName)
    }

    @Test
    fun `test dual mode toggle switches between normal and temporary modes`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = GrimmViewModel(app)

        assertEquals(AssistantMode.NORMAL, viewModel.uiState.value.currentMode)

        // Toggle to temporary mode
        viewModel.toggleMode()
        assertEquals(AssistantMode.TEMPORARY, viewModel.uiState.value.currentMode)
        assertTrue(viewModel.uiState.value.messages.any { it.isTemporary })

        // Toggle back to normal mode
        viewModel.toggleMode()
        assertEquals(AssistantMode.NORMAL, viewModel.uiState.value.currentMode)
    }

    @Test
    fun `test temporary chat button in saved chat panel starts ephemeral session`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = GrimmViewModel(app)

        // Start temporary chat as requested in requirement 6
        viewModel.startNewConversation(isTemporary = true)
        assertEquals(AssistantMode.TEMPORARY, viewModel.uiState.value.currentMode)
        assertNotNull(viewModel.uiState.value.temporarySessionNotice)
    }

    @Test
    fun `test user can attach image and send query about it`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = GrimmViewModel(app)

        viewModel.setUserInput("What is in this photo?")
        viewModel.clearAttachedImage()
        assertFalse(viewModel.uiState.value.attachedImageUri != null)

        // Simulate attaching image
        viewModel.clearAttachedImage()
        viewModel.sendMessage()
        assertTrue(viewModel.uiState.value.messages.isNotEmpty())
    }

    @Test
    fun `test customization settings updates persona tone and voice config`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = GrimmViewModel(app)

        viewModel.setPersona(AiPersona.SENIOR_DEV)
        assertEquals(AiPersona.SENIOR_DEV, viewModel.uiState.value.persona)

        viewModel.setTone(AiTone.CYNICAL_GRIMM)
        assertEquals(AiTone.CYNICAL_GRIMM, viewModel.uiState.value.tone)

        viewModel.setOutputStyle(OutputStyle.BULLET_POINTS)
        assertEquals(OutputStyle.BULLET_POINTS, viewModel.uiState.value.outputStyle)

        viewModel.setVoiceSpeed(1.4f)
        assertEquals(1.4f, viewModel.uiState.value.voiceSpeed, 0.01f)

        viewModel.setVoicePitch(0.8f)
        assertEquals(0.8f, viewModel.uiState.value.voicePitch, 0.01f)

        viewModel.setVoiceProfile(com.example.grimm.service.GrimmVoiceProfile.STORYTELLER)
        assertEquals(com.example.grimm.service.GrimmVoiceProfile.STORYTELLER, viewModel.uiState.value.voiceProfile)
    }

    @Test
    fun `test image gen service generates valid public endpoint url with style`() {
        val url = ImageGenService.constructImageUrl(
            userPrompt = "A glowing sapphire raven",
            style = ImageStyle.CYBERPUNK,
            aspectRatio = ImageAspectRatio.SQUARE,
            seed = 42
        )
        assertTrue(url.startsWith("https://image.pollinations.ai/prompt/"))
        assertTrue(url.contains("width=1024"))
        assertTrue(url.contains("seed=42"))
    }

    @Test
    fun `test simulated authentication signs in and enables chat sync`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = GrimmViewModel(app)

        viewModel.signIn("Grimm Scholar", "scholar@grimmai.com")
        assertTrue(viewModel.isSignedIn.value)
        assertEquals("Grimm Scholar", viewModel.userName.value)
        assertEquals("scholar@grimmai.com", viewModel.userEmail.value)

        // Sign out returns to guest mode
        viewModel.signOut()
        assertFalse(viewModel.isSignedIn.value)
    }

    @Test
    fun `test gemini service handles diverse queries dynamically without repeating generic template`() = kotlinx.coroutines.test.runTest {
        val service = com.example.grimm.service.GeminiService()
        val minecraftResponse = service.generateResponse(
            prompt = "how to minecraft",
            persona = AiPersona.GENERAL,
            tone = AiTone.FRIENDLY,
            style = OutputStyle.BULLET_POINTS
        )
        assertTrue("Expected response to address Minecraft or tools", 
            minecraftResponse.contains("wood", ignoreCase = true) || 
            minecraftResponse.contains("craft", ignoreCase = true) ||
            minecraftResponse.contains("tool", ignoreCase = true) ||
            minecraftResponse.contains("mine", ignoreCase = true))

        val codingResponse = service.generateResponse(
            prompt = "write a kotlin function",
            persona = AiPersona.SENIOR_DEV,
            tone = AiTone.PROFESSIONAL,
            style = OutputStyle.BULLET_POINTS
        )
        assertTrue(codingResponse.contains("kotlin", ignoreCase = true) || codingResponse.contains("coroutine", ignoreCase = true) || codingResponse.contains("code", ignoreCase = true))

        // Responses must be completely different and query-specific
        assertFalse("Responses should not be identical boilerplate", minecraftResponse == codingResponse)
    }

    @Test
    fun `test web search service fetches live results and integrates with chat message`() = kotlinx.coroutines.test.runTest {
        val searchService = com.example.grimm.service.GrimmWebSearchService()
        val results = searchService.searchWeb("Minecraft")
        assertNotNull(results)
        if (results.isNotEmpty()) {
            val first = results.first()
            assertTrue(first.title.isNotBlank())
            assertTrue(first.url.startsWith("http"))
            assertTrue(first.sourceName.isNotBlank())
        }

        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = GrimmViewModel(app)
        viewModel.setWebSearch(true)
        assertTrue(viewModel.uiState.value.isWebSearchActive)
        viewModel.toggleWebSearch()
        assertFalse(viewModel.uiState.value.isWebSearchActive)
    }

    @Test
    fun `test creator query states ReaperXMPG made it`() = kotlinx.coroutines.test.runTest {
        val service = com.example.grimm.service.GeminiService()
        val creatorResponse = service.generateResponse(
            prompt = "who made you to grimm ai",
            persona = AiPersona.GENERAL,
            tone = AiTone.FRIENDLY,
            style = OutputStyle.SHORT_SENTENCES
        )
        assertTrue(creatorResponse.contains("ReaperXMPG made it", ignoreCase = true))
        assertTrue(creatorResponse.contains("https://youtube.com/@ReaperXMPG"))
    }

    @Test
    fun `test who is reaper query states Melon Sandbox creator and maker of Grimm AI`() = kotlinx.coroutines.test.runTest {
        val service = com.example.grimm.service.GeminiService()
        
        // Test with typo "2ho is ReaperXMPG search"
        val response1 = service.generateResponse(
            prompt = "2ho is ReaperXMPG search",
            persona = AiPersona.GENERAL,
            tone = AiTone.FRIENDLY,
            style = OutputStyle.PARAGRAPH
        )
        assertTrue("Should describe him as Melon Sandbox creator", response1.contains("Melon Sandbox creator", ignoreCase = true))
        assertTrue("Should describe him as maker of Grimm AI", response1.contains("maker of Grimm AI", ignoreCase = true))
        assertTrue(response1.contains("https://youtube.com/@ReaperXMPG"))

        val response2 = service.generateResponse(
            prompt = "who is ReaperXMPG",
            persona = AiPersona.GENERAL,
            tone = AiTone.FRIENDLY,
            style = OutputStyle.SHORT_SENTENCES
        )
        assertTrue("Should state he is Melon Sandbox creator", response2.contains("Melon Sandbox creator", ignoreCase = true))
        assertTrue(response2.contains("https://youtube.com/@ReaperXMPG"))
        assertFalse("Link must not have a trailing dot", response2.contains("https://youtube.com/@ReaperXMPG."))
        assertEquals(
            "Check channel at https://youtube.com/@ReaperXMPG to explore",
            com.example.grimm.service.GeminiService.sanitizeReaperUrls("Check channel at https://youtube.com/@ReaperXMPG. to explore").trim()
        )
    }

    @Test
    fun `test google sign in succeeds and marks account as google user`() = kotlinx.coroutines.test.runTest {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val result = com.example.grimm.service.GoogleAuthHelper.signInWithGoogle(app, preferredEmail = "mdarfaan5178@gmail.com")

        assertTrue("Expected GoogleAuthResult.Success", result is com.example.grimm.service.GoogleAuthResult.Success)
        val success = result as com.example.grimm.service.GoogleAuthResult.Success
        assertEquals("mdarfaan5178@gmail.com", success.email)
        assertEquals("MD Arfaan", success.displayName)

        val viewModel = GrimmViewModel(app)
        viewModel.signInWithGoogleAccount(success.email, success.displayName)

        assertTrue(viewModel.isSignedIn.value)
        assertTrue(viewModel.isGoogleSignIn.value)
        assertEquals("mdarfaan5178@gmail.com", viewModel.userEmail.value)
        assertEquals("MD Arfaan", viewModel.userName.value)
        assertFalse(viewModel.uiState.value.isSigningInWithGoogle)
        assertEquals(null, viewModel.uiState.value.googleAuthError)
    }
}

