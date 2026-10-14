package com.example.grimm.service

import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlin.random.Random

enum class ImageStyle(val label: String, val promptSuffix: String) {
    NONE("Original", ""),
    GRIMM_DARK("Dark Fantasy", ", dark fantasy, gothic atmosphere, intricate mystical illumination, 8k digital painting"),
    CYBERPUNK("Cyberpunk", ", cyberpunk neon aesthetic, glowing cobalt and electric blue highlights, futuristic, highly detailed"),
    CINEMATIC("Cinematic Photo", ", 35mm film photography, cinematic lighting, ultra-realistic, natural shadows, depth of field"),
    ANIME("Anime Studio", ", modern high-detail anime style, vibrant art, clean line work, studio quality"),
    DIGITAL_ART("Digital Art", ", vibrant digital concept art, trending on artstation, masterpiece, dramatic lighting"),
    MINIMALIST("Minimalist", ", clean vector art, flat color palette, sleek geometry, modern graphic design")
}

enum class ImageAspectRatio(val label: String, val width: Int, val height: Int) {
    SQUARE("1:1 Square", 1024, 1024),
    LANDSCAPE("16:9 Wide", 1280, 720),
    PORTRAIT("9:16 Story", 720, 1280)
}

object ImageGenService {
    fun constructImageUrl(
        userPrompt: String,
        style: ImageStyle = ImageStyle.NONE,
        aspectRatio: ImageAspectRatio = ImageAspectRatio.SQUARE,
        seed: Int = Random.nextInt(1000, 999999)
    ): String {
        val enhancedPrompt = (userPrompt.trim() + style.promptSuffix).trim()
        val encodedPrompt = URLEncoder.encode(enhancedPrompt, StandardCharsets.UTF_8.toString())
            .replace("+", "%20")
        return "https://image.pollinations.ai/prompt/$encodedPrompt?width=${aspectRatio.width}&height=${aspectRatio.height}&seed=$seed&nologo=true"
    }

    val samplePrompts = listOf(
        "A mystical raven perched on an ancient blue crystal tablet in a forgotten library",
        "Cybernetic neural core glowing with cobalt energy and intricate gold runes",
        "A medieval alchemist laboratory with glowing neon vials and starry constellations",
        "A cyberpunk detective walking through rainy neon streets under floating holograms",
        "An ancient enchanted forest with luminescent sapphire flowers and whispering mist"
    )
}
