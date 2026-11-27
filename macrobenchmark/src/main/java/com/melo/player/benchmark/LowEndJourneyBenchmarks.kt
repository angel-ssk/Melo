package com.melo.player.benchmark

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Low-end device journey benchmarks for Melo 1.0.
 *
 * These three journeys are the ones a Tecno Spark Go actually feels:
 *   - coldStartupLowEnd: time to first frame on a fresh process
 *   - searchLowEnd: search FAB → typed query → results list
 *   - playerLowEnd: opening a song and rendering the full player
 *
 * Run both distributions (the generated Baseline Profile is flavor-specific):
 *
 *   .\gradlew.bat :macrobenchmark:connectedGithubBenchmarkAndroidTest
 *       "-Pandroid.testInstrumentationRunnerArguments.class=com.melo.player.benchmark.LowEndJourneyBenchmarks"
 *       --no-daemon
 *
 *   .\gradlew.bat :macrobenchmark:connectedFdroidBenchmarkAndroidTest
 *       "-Pandroid.testInstrumentationRunnerArguments.class=com.melo.player.benchmark.LowEndJourneyBenchmarks"
 *       --no-daemon
 */
@RunWith(AndroidJUnit4::class)
class LowEndJourneyBenchmarks {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    private val profileMode = CompilationMode.Partial(
        baselineProfileMode = BaselineProfileMode.UseIfAvailable,
        warmupIterations = 1,
    )

    @Test
    fun coldStartupLowEnd() = benchmarkRule.measureRepeated(
        packageName = APP_PACKAGE,
        metrics = listOf(StartupTimingMetric(), FrameTimingMetric()),
        compilationMode = profileMode,
        startupMode = StartupMode.COLD,
        iterations = 6,
        setupBlock = { grantMeloRuntimePermissions(APP_PACKAGE) },
    ) {
        startActivityAndWait()
        device.waitForIdle(3_000)
    }

    @Test
    fun searchLowEnd() = benchmarkRule.measureRepeated(
        packageName = APP_PACKAGE,
        metrics = listOf(FrameTimingMetric()),
        compilationMode = profileMode,
        startupMode = StartupMode.WARM,
        iterations = 6,
        setupBlock = { grantMeloRuntimePermissions(APP_PACKAGE) },
    ) {
        startActivityAndWait()
        device.waitForIdle(2_000)

        val search = device.findObject(By.desc("Search"))
            ?: device.findObject(By.desc("Buscar"))
            ?: device.findObject(By.descContains("search"))
            ?: device.findObject(By.descContains("Buscar"))
            ?: return@measureRepeated
        search.click()
        device.waitForIdle(800)

        device.findObject(By.clazz("android.widget.EditText"))?.click()
        device.findObject(By.clazz("android.widget.EditText"))?.text = "a"
        device.waitForIdle(1_000)

        repeat(2) {
            val x = device.displayWidth / 2
            val top = (device.displayHeight * 0.3f).toInt()
            val bottom = (device.displayHeight * 0.75f).toInt()
            device.swipe(x, bottom, x, top, 18)
            device.waitForIdle(250)
        }
    }

    @Test
    fun playerLowEnd() = benchmarkRule.measureRepeated(
        packageName = APP_PACKAGE,
        metrics = listOf(FrameTimingMetric()),
        compilationMode = profileMode,
        startupMode = StartupMode.WARM,
        iterations = 6,
        setupBlock = { grantMeloRuntimePermissions(APP_PACKAGE) },
    ) {
        startActivityAndWait()
        device.waitForIdle(2_000)

        val song = device.findObject(By.clickable(true)) ?: return@measureRepeated
        song.click()
        device.waitForIdle(1_500)

        val player = device.findObject(By.descContains("mini"))
            ?: device.findObject(By.descContains("reproductor"))
        player?.click()
        device.waitForIdle(1_500)

        // Exercise artwork, controls and lyrics on the full player.
        repeat(2) {
            val x = device.displayWidth / 2
            val top = (device.displayHeight * 0.25f).toInt()
            val bottom = (device.displayHeight * 0.75f).toInt()
            device.swipe(x, bottom, x, top, 18)
            device.waitForIdle(300)
        }
    }

    private companion object {
        const val APP_PACKAGE = "com.melo.player"
    }
}
