package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.grimm.ui.screens.GrimmMainScreen
import com.example.ui.theme.GrimmOffWhite
import com.example.ui.theme.MyApplicationTheme
import com.example.grimm.viewmodel.GrimmViewModel
import com.github.javiersantos.appupdater.AppUpdater
import com.github.javiersantos.appupdater.enums.UpdateFrom

class MainActivity : ComponentActivity() {
    private val viewModel: GrimmViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppUpdater(this)
    .setUpdateFrom(UpdateFrom.GITHUB)
    .setGitHubUserAndRepo("ReaperXMPG", "Grimm-AI")
    .start()
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = GrimmOffWhite
                ) {
                    GrimmMainScreen(viewModel = viewModel)
                }
            }
        }
    }
}
