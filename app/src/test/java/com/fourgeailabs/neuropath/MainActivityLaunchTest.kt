package com.fourgeailabs.neuropath

import androidx.test.core.app.ActivityScenario
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.junit.Assert.assertNotNull
import com.fourgeailabs.neuropath.ui.AppScreen

@RunWith(RobolectricTestRunner::class)
class MainActivityLaunchTest {

    @Test
    fun testMainActivityLaunchesWithoutCrashing() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        scenario.onActivity { activity ->
            assertNotNull(activity)
        }
        scenario.close()
    }

    @Test
    fun testAllScreensRenderWithoutCrashing() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        scenario.onActivity { activity ->
            val vmField = MainActivity::class.java.getDeclaredFields().firstOrNull { 
                it.name.startsWith("viewModel") 
            } ?: MainActivity::class.java.getDeclaredFields().first { 
                it.type.name.contains("NeuroPathViewModel") || it.type.name.contains("Lazy")
            }
            vmField.isAccessible = true
            val prop = vmField.get(activity)
            val vm = if (prop is Lazy<*>) prop.value as com.fourgeailabs.neuropath.ui.NeuroPathViewModel else prop as com.fourgeailabs.neuropath.ui.NeuroPathViewModel

            AppScreen.entries.forEach { screen ->
                vm.navigateTo(screen)
            }
        }
        scenario.close()
    }
}

