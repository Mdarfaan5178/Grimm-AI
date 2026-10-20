package com.example.grimm.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.grimm.model.AiPersona
import com.example.grimm.model.AiTone
import com.example.ui.theme.GrimmBorder
import com.example.ui.theme.GrimmCyan
import com.example.ui.theme.GrimmDarkBlue
import com.example.ui.theme.GrimmLightBlue
import com.example.ui.theme.GrimmLightBlueSoft
import com.example.ui.theme.GrimmOffWhite
import com.example.ui.theme.GrimmSurface
import com.example.ui.theme.GrimmTextMuted
import com.example.grimm.service.GrimmVoiceProfile
import com.example.ui.theme.GrimmTextPrimary
import com.example.ui.theme.GrimmTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrimmSettingsDialog(
    selectedPersona: AiPersona,
    onPersonaChange: (AiPersona) -> Unit,
    selectedTone: AiTone,
    onToneChange: (AiTone) -> Unit,
    selectedVoiceProfile: GrimmVoiceProfile,
    onVoiceProfileChange: (GrimmVoiceProfile) -> Unit,
    voiceSpeed: Float,
    onVoiceSpeedChange: (Float) -> Unit,
    voicePitch: Float,
    onVoicePitchChange: (Float) -> Unit,
    autoTts: Boolean,
    onAutoTtsChange: (Boolean) -> Unit,
    onTestVoice: () -> Unit,
    onClearAllHistory: () -> Unit,
    onDismiss: () -> Unit,
    userName: String,
    onUserNameChange: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = GrimmOffWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(GrimmDarkBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = GrimmCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Settings",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = GrimmDarkBlue
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close settings")
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Username Section
            Text(
                text = "USER PROFILE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = GrimmTextSecondary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = userName,
                onValueChange = onUserNameChange,
                label = { Text("Display Name") },
                modifier = Modifier.fillMaxWidth().testTag("username_edit_field")
            )
            
            Spacer(modifier = Modifier.height(18.dp))

            // AI Persona Section
            Text(
                text = "AI PERSONA",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = GrimmTextSecondary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AiPersona.values().forEach { persona ->
                    val isSelected = selectedPersona == persona
                    Surface(
                        onClick = { onPersonaChange(persona) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) GrimmLightBlueSoft else GrimmSurface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) GrimmLightBlue else GrimmBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("persona_option_${persona.name}")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) GrimmLightBlue else GrimmBorder),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else GrimmTextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = persona.label,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) GrimmDarkBlue else GrimmTextPrimary
                                )
                                Text(
                                    text = persona.role,
                                    fontSize = 11.sp,
                                    color = GrimmTextSecondary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Tone Section
            Text(
                text = "CONVERSATIONAL TONE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = GrimmTextSecondary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AiTone.values().forEach { tone ->
                    val isSelected = selectedTone == tone
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) GrimmDarkBlue else GrimmSurface)
                            .border(1.dp, if (isSelected) GrimmDarkBlue else GrimmBorder, RoundedCornerShape(12.dp))
                            .clickable { onToneChange(tone) }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Column {
                            Text(
                                text = tone.label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else GrimmTextPrimary
                            )
                            Text(
                                text = tone.description,
                                fontSize = 10.sp,
                                color = if (isSelected) GrimmCyan else GrimmTextMuted
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Voice Configuration Section
            Text(
                text = "VOICE ENGINE & SYNTHESIS (TTS)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = GrimmTextSecondary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = GrimmSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, GrimmBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Auto-read switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Auto-Read Responses Aloud",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GrimmDarkBlue
                            )
                            Text(
                                text = "Narrate assistant responses automatically",
                                fontSize = 11.sp,
                                color = GrimmTextSecondary
                            )
                        }
                        Switch(
                            checked = autoTts,
                            onCheckedChange = onAutoTtsChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = GrimmLightBlue
                            ),
                            modifier = Modifier.testTag("auto_tts_switch")
                        )
                    }

                    HorizontalDivider(
                        color = GrimmBorder,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    // Voice Profiles
                    Text(
                        text = "VOICE PROFILE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrimmTextSecondary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GrimmVoiceProfile.values().forEach { profile ->
                            val isSelected = selectedVoiceProfile == profile
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) GrimmLightBlue else GrimmOffWhite)
                                    .border(1.dp, if (isSelected) GrimmLightBlue else GrimmBorder, RoundedCornerShape(10.dp))
                                    .clickable { onVoiceProfileChange(profile) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Column {
                                    Text(
                                        text = profile.title,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else GrimmDarkBlue
                                    )
                                    Text(
                                        text = profile.subtitle,
                                        fontSize = 10.sp,
                                        color = if (isSelected) Color.White.copy(alpha = 0.85f) else GrimmTextSecondary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Speech Rate / Speed
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Speech Rate (Speed)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = GrimmTextPrimary
                        )
                        Text(
                            text = String.format("%.2fx", voiceSpeed),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GrimmLightBlue
                        )
                    }
                    Slider(
                        value = voiceSpeed,
                        onValueChange = onVoiceSpeedChange,
                        valueRange = 0.8f..1.3f,
                        steps = 9,
                        colors = SliderDefaults.colors(
                            thumbColor = GrimmLightBlue,
                            activeTrackColor = GrimmLightBlue
                        ),
                        modifier = Modifier.testTag("voice_speed_slider")
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Voice Pitch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Voice Pitch (Resonance)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = GrimmTextPrimary
                        )
                        Text(
                            text = String.format("%.2fx", voicePitch),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GrimmLightBlue
                        )
                    }
                    Slider(
                        value = voicePitch,
                        onValueChange = onVoicePitchChange,
                        valueRange = 0.85f..1.25f,
                        steps = 7,
                        colors = SliderDefaults.colors(
                            thumbColor = GrimmLightBlue,
                            activeTrackColor = GrimmLightBlue
                        ),
                        modifier = Modifier.testTag("voice_pitch_slider")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Voice Actions: Calibrate to Optimal & Test
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                onVoicePitchChange(selectedVoiceProfile.defaultPitch)
                                onVoiceSpeedChange(selectedVoiceProfile.defaultSpeed)
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("reset_voice_defaults_button")
                        ) {
                            Text("Optimal Reset", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = onTestVoice,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GrimmLightBlue,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("test_voice_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Test Voice", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Danger zone: Clear saved history
            OutlinedButton(
                onClick = onClearAllHistory,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFEF4444)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("clear_history_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Clear All Saved Conversations", fontSize = 12.sp)
            }
        }
    }
}
