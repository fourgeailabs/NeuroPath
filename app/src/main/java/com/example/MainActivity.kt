package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.AppScreen
import com.example.ui.NeuroPathViewModel
import com.example.ui.components.TopSensoryBar
import com.example.ui.screens.AvatarShopScreen
import com.example.ui.screens.BreathingGuideScreen
import com.example.ui.screens.CreativeStudioScreen
import com.example.ui.screens.FidgetPopItScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MasteryJourneyScreen
import com.example.ui.screens.NeuroBuddyChatScreen
import com.example.ui.screens.OceanReadingGameScreen
import com.example.ui.screens.ParentDashboardScreen
import com.example.ui.screens.ParentPinGateScreen
import com.example.ui.screens.TeachLessonScreen
import com.example.ui.theme.getDyslexiaTypography
import com.example.ui.theme.getThemeColorScheme

class MainActivity : ComponentActivity() {

    private val viewModel: NeuroPathViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val profile by viewModel.currentProfile.collectAsState()
            val theme = viewModel.getActiveTheme()
            val currentScreen by viewModel.currentScreen.collectAsState()

            val colorScheme = getThemeColorScheme(
                worldTheme = theme,
                contrastMode = profile.highContrastMode
            )
            val typography = getDyslexiaTypography(profile.dyslexiaFontEnabled)

            MaterialTheme(
                colorScheme = colorScheme,
                typography = typography
            ) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopSensoryBar(viewModel = viewModel)
                    }
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        when (currentScreen) {
                            AppScreen.HOME -> HomeScreen(viewModel = viewModel)
                            AppScreen.TEACH_LESSON -> TeachLessonScreen(viewModel = viewModel)
                            AppScreen.MASTERY_JOURNEY -> MasteryJourneyScreen(viewModel = viewModel)
                            AppScreen.OCEAN_GAME -> OceanReadingGameScreen(viewModel = viewModel)
                            AppScreen.CREATIVE_STUDIO -> CreativeStudioScreen(viewModel = viewModel)
                            AppScreen.FIDGET_POPIT -> FidgetPopItScreen(viewModel = viewModel)
                            AppScreen.BREATHING_GUIDE -> BreathingGuideScreen(viewModel = viewModel)
                            AppScreen.NEURO_BUDDY_CHAT -> NeuroBuddyChatScreen(viewModel = viewModel)
                            AppScreen.AVATAR_SHOP -> AvatarShopScreen(viewModel = viewModel)
                            AppScreen.PARENT_PIN_GATE -> ParentPinGateScreen(viewModel = viewModel)
                            AppScreen.PARENT_DASHBOARD -> ParentDashboardScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
