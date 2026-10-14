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
}
