package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.BackgroundWhite
import com.example.ui.theme.ProjectMassTheme
import com.example.ui.viewmodel.MassViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProjectMassTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BackgroundWhite
                ) {
                    val viewModel: MassViewModel = viewModel()
                    var showSplash by remember { mutableStateOf(true) }
                    val messages by viewModel.messages.collectAsState()

                    BackHandler(enabled = !showSplash && messages.isNotEmpty()) {
                        viewModel.createNewSession()
                    }

                    AnimatedContent(
                        targetState = showSplash,
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        },
                        label = "screen_transition"
                    ) { isSplash ->
                        if (isSplash) {
                            SplashScreen(
                                onDismissSplash = { showSplash = false }
                            )
                        } else {
                            HomeScreen(
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }
}
