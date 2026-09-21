package com.fourgeailabs.neuropath

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.fourgeailabs.neuropath.ui.AppScreen
import com.fourgeailabs.neuropath.ui.NeuroPathViewModel
import com.fourgeailabs.neuropath.ui.components.TopSensoryBar
import com.fourgeailabs.neuropath.ui.screens.AvatarShopScreen
import com.fourgeailabs.neuropath.ui.screens.BreathingGuideScreen
import com.fourgeailabs.neuropath.ui.screens.ChildProfileSetupScreen
import com.fourgeailabs.neuropath.ui.screens.CreativeStudioScreen
import com.fourgeailabs.neuropath.ui.screens.FidgetPopItScreen
import com.fourgeailabs.neuropath.ui.screens.HomeScreen
import com.fourgeailabs.neuropath.ui.screens.LanguageSelectionScreen
import com.fourgeailabs.neuropath.ui.screens.MasteryJourneyScreen
import com.fourgeailabs.neuropath.ui.screens.NeuroBuddyChatScreen
import com.fourgeailabs.neuropath.ui.screens.OceanReadingGameScreen
import com.fourgeailabs.neuropath.ui.screens.ParentDashboardScreen
import com.fourgeailabs.neuropath.ui.screens.ParentPinGateScreen
import com.fourgeailabs.neuropath.ui.screens.ParentPinSetupScreen
import com.fourgeailabs.neuropath.ui.screens.ProfileSelectionScreen
import com.fourgeailabs.neuropath.ui.screens.TeachLessonScreen
import com.fourgeailabs.neuropath.ui.screens.TermsAndConditionsScreen
import com.fourgeailabs.neuropath.ui.theme.getDyslexiaTypography
import com.fourgeailabs.neuropath.ui.theme.getThemeColorScheme

class MainActivity : ComponentActivity() {

    private val viewModel: NeuroPathViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        androidx.core.view.WindowCompat.getInsetsController(window, window.decorView)?.isAppearanceLightStatusBars = true

        setContent {
            val profile by viewModel.currentProfile.collectAsState()
            val neuroTheme = viewModel.getActiveNeuroTheme()
            val currentScreen by viewModel.currentScreen.collectAsState()

            // System Back Button Behavior: Navigate back one page instead of closing the app
            BackHandler(enabled = true) {
                val handled = viewModel.navigateBack()
                if (!handled) {
                    // Only finish if at initial screen or root
                    finish()
                }
            }

            val colorScheme = getThemeColorScheme(
                neuroTheme = neuroTheme,
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
                        if (currentScreen != AppScreen.LANGUAGE_SELECTION &&
                            currentScreen != AppScreen.TERMS_AND_CONDITIONS &&
                            currentScreen != AppScreen.PARENT_PIN_SETUP &&
                            currentScreen != AppScreen.CHILD_PROFILE_SETUP &&
                            currentScreen != AppScreen.PROFILE_SELECTION &&
                            currentScreen != AppScreen.PARENT_DASHBOARD &&
                            currentScreen != AppScreen.PARENT_PIN_GATE &&
                            currentScreen != AppScreen.NEURO_BUDDY_CHAT) {
                            TopSensoryBar(viewModel = viewModel)
                        }
                    }
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        when (currentScreen) {
                            AppScreen.LANGUAGE_SELECTION -> LanguageSelectionScreen(viewModel = viewModel)
                            AppScreen.TERMS_AND_CONDITIONS -> TermsAndConditionsScreen(viewModel = viewModel)
                            AppScreen.PARENT_PIN_SETUP -> ParentPinSetupScreen(viewModel = viewModel)
                            AppScreen.CHILD_PROFILE_SETUP -> ChildProfileSetupScreen(viewModel = viewModel)
                            AppScreen.PROFILE_SELECTION -> ProfileSelectionScreen(viewModel = viewModel)
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
