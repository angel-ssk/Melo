/*
 * SPDX-FileCopyrightText: 2024-2026 Anjishnu Nandi <https://github.com/angel-ssk>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.melo.player

import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.content.ComponentCallbacks2
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import coil.Coil
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import okhttp3.Dispatcher
import com.melo.player.infrastructure.widget.glance.GlanceShapeBitmaps
import com.melo.player.infrastructure.widget.glance.MeloCookieWidget
import com.melo.player.infrastructure.widget.glance.MeloMusicWidget
import com.melo.player.shared.data.model.AppSettings
import com.melo.player.util.ANRWatchdog
import com.melo.player.util.CacheManager
import com.melo.player.util.CrashReporter
import com.melo.player.util.MediaUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * Custom Application class for Melo Music Player.
 * Handles initialization of:
 * - AppSettings
 * - CrashReporter
 * - NetworkClient
 * - LeakCanary (debug builds)
 * - ANR Watchdog (debug builds)
 */
class MeloApplication : Application(), ImageLoaderFactory {
    
    companion object {
        private const val TAG = "MeloApplication"

        
        // Static reference to the application instance
        // Using a static reference is safe for Application class
        lateinit var instance: MeloApplication
            private set

        /** True on low-RAM devices: used to skip expensive blur/ambient effects. */
        var isLowRam: Boolean = false
            private set

        @Volatile
        var isForeground: Boolean = true
            private set

        /** Observable foreground state so decorative animations can stop in the background. */
        val foregroundFlow = MutableStateFlow(true)
    }

    private fun setForeground(value: Boolean) {
        isForeground = value
        foregroundFlow.value = value
    }
    
    private var anrWatchdog: ANRWatchdog? = null
    
    override fun onCreate() {
        super.onCreate()
        
        instance = this
        isLowRam = (getSystemService(android.content.Context.ACTIVITY_SERVICE) as android.app.ActivityManager).isLowRamDevice

        // Pause decorative animations while the app is in the background.
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            private var started = 0
            override fun onActivityStarted(activity: android.app.Activity) {
                started++
                setForeground(true)
            }
            override fun onActivityStopped(activity: android.app.Activity) {
                started--
                if (started <= 0) setForeground(false)
            }
            override fun onActivityCreated(activity: android.app.Activity, savedInstanceState: android.os.Bundle?) {}
            override fun onActivityResumed(activity: android.app.Activity) {}
            override fun onActivityPaused(activity: android.app.Activity) {}
            override fun onActivitySaveInstanceState(activity: android.app.Activity, outState: android.os.Bundle) {}
            override fun onActivityDestroyed(activity: android.app.Activity) {}
        })

        Log.d(TAG, "═══════════════════════════════════════════════════")
        Log.d(TAG, "MeloApplication onCreate")
        Log.d(TAG, "Build Type: ${BuildConfig.BUILD_TYPE}")
        Log.d(TAG, "Version: ${BuildConfig.VERSION_NAME}")
        Log.d(TAG, "═══════════════════════════════════════════════════")
        
        // Initialize AppSettings early (singleton, uses application context)
        val settings = AppSettings.getInstance(applicationContext)
        Log.d(TAG, "✓ AppSettings initialized")

        // Apply the user's theme preference to the system configuration so the
        // system splash screen and window background match the app theme
        // (light in light mode, dark in dark mode) instead of always being dark.
        val nightMode = if (settings.useSystemTheme.value) {
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        } else if (settings.darkMode.value) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
        Log.d(TAG, "✓ Applied night mode: $nightMode")
        
        // Deferred startup: let the first frame paint before doing non-critical work.
        // This matters on low-RAM phones where synchronous init before Activity creation
        // is what makes the app feel slow to open.
        android.view.Choreographer.getInstance().postFrameCallback {
            if (BuildConfig.DEBUG) {
                configureLeakCanary()
                startANRWatchdog()
            }
            CoroutineScope(Dispatchers.IO).launch {
                CrashReporter.init(this@MeloApplication)
                Log.d(TAG, "✓ CrashReporter initialized (deferred)")

                com.melo.player.network.NetworkClient.initialize(settings)
                Log.d(TAG, "✓ NetworkClient initialized (deferred)")

                trimCacheOnStartup()
            }
        }
        
        Log.d(TAG, "MeloApplication initialization complete")
    }

    /**
     * Trim caches at most once per day, off the main thread, so storage stays
     * within the user's chosen limit even after a force-stop.
     */
    private suspend fun trimCacheOnStartup() {
        try {
            val trimPrefs = getSharedPreferences("cache_trim", android.content.Context.MODE_PRIVATE)
            val lastTrimMs = trimPrefs.getLong("last_startup_trim_ms", 0L)
            val dayMs = 24L * 60 * 60 * 1000
            if (System.currentTimeMillis() - lastTrimMs >= dayMs) {
                trimPrefs.edit { putLong("last_startup_trim_ms", System.currentTimeMillis()) }
                CacheManager.autoTrimCache(applicationContext, currentMaxCacheSize())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during startup cache trim", e)
        }
    }

    /**
     * Returns the user-configured max cache size or a sane fallback if
     * settings are unavailable.
     */
    private fun currentMaxCacheSize(): Long {
        return try {
            AppSettings.getInstance(this).maxCacheSize.value
        } catch (e: Exception) {
            300L * 1024 * 1024
        }
    }

    /**
     * Bounded Coil ImageLoader used app-wide with on-demand audio artwork decoding.
     * The factory defaults keep the disk cache at ~2% of total disk space and the memory
     * cache at 25% of heap, which is far too large for an offline music app — bound both explicitly.
     */
    override fun newImageLoader(): ImageLoader {
        // Adapt image-caching and concurrency to the device class: low-RAM devices
        // get smaller caches and fewer parallel decodes to avoid GC pressure/jank,
        // while high-end devices keep larger caches for fewer network hits.
        val activityManager = getSystemService(android.content.Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        val lowRam = activityManager.isLowRamDevice
        val memoryClassMb = activityManager.memoryClass
        val maxRequests = if (lowRam) 4 else if (memoryClassMb >= 256) 20 else 12
        val maxRequestsPerHost = if (lowRam) 2 else 4
        val memoryCachePercent = if (lowRam) 0.08 else if (memoryClassMb >= 256) 0.18 else 0.14
        val diskCacheBytes = if (lowRam) 48L * 1024 * 1024 else 128L * 1024 * 1024

        val imageOkHttpClient = com.melo.player.features.streaming.data.provider.UserTrustManager
            .buildUserTrustingHttpClientBuilder()
            .dispatcher(Dispatcher().apply {
                this.maxRequests = maxRequests
                this.maxRequestsPerHost = maxRequestsPerHost
            })
            .build()

        return ImageLoader.Builder(this)
            .okHttpClient(imageOkHttpClient)
            .components {
                add(com.melo.player.util.coil.AudioArtworkKeyer())
                add(com.melo.player.util.coil.StreamingArtworkKeyer())
                add(com.melo.player.util.coil.StreamingArtworkStringKeyer())
                add(com.melo.player.util.coil.AudioArtworkFetcher.Factory(applicationContext))
            }
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(memoryCachePercent)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(diskCacheBytes)
                    .build()
            }
            .crossfade(!lowRam)
            .build()
    }
    
    /**
     * Configure LeakCanary for optimal memory leak detection
     */
    private fun configureLeakCanary() {
        try {
            // LeakCanary 2.x auto-configures itself, but we can still apply debug-only tuning.
            // Reflection keeps main source free from debugImplementation class references.
            val debugConfigClass = Class.forName("com.melo.player.debug.LeakCanaryDebugConfig")
            val applyMethod = debugConfigClass.getDeclaredMethod("applyKnownReferenceMatchers")
            applyMethod.invoke(null)

            Log.d(TAG, "✓ LeakCanary configured (auto-init + debug matcher tuning)")
        } catch (_: ClassNotFoundException) {
            Log.d(TAG, "✓ LeakCanary configured (auto-init)")
        } catch (e: Exception) {
            Log.e(TAG, "Error configuring LeakCanary", e)
        }
    }
    
    /**
     * Start ANR watchdog to monitor UI thread responsiveness
     */
    private fun startANRWatchdog() {
        try {
            // Start with 5 second timeout (standard ANR threshold)
            anrWatchdog = ANRWatchdog(timeoutMs = 5000).apply {
                start()
            }
            Log.d(TAG, "✓ ANR Watchdog started")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting ANR Watchdog", e)
        }
    }
    
    override fun onTerminate() {
        Log.d(TAG, "MeloApplication onTerminate")
        
        // Stop ANR watchdog
        anrWatchdog?.stopWatching()
        anrWatchdog = null
        
        super.onTerminate()
    }
    
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        
        val levelName = when {
            level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> "UI_HIDDEN"
            level == ComponentCallbacks2.TRIM_MEMORY_BACKGROUND -> "BACKGROUND"
            level == 5 -> "RUNNING_MODERATE"
            level == 10 -> "RUNNING_LOW"
            level == 15 -> "RUNNING_CRITICAL"
            level == 60 -> "MODERATE"
            level == 80 -> "COMPLETE"
            else -> "UNKNOWN($level)"
        }
        
        Log.w(TAG, "onTrimMemory: $levelName")
        
        // Trim levels are inverted: lower numbers mean MORE pressure (5 = critical,
        // 10 = low, 15 = moderate). Release image caches whenever the app is under
        // real pressure OR fully backgrounded — this is the main OOM fix when many
        // apps are open.
        val isBackgroundOrCritical = level >= ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN
        if (isBackgroundOrCritical) {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    Coil.imageLoader(applicationContext).memoryCache?.clear()
                } catch (e: Exception) {
                    Log.w(TAG, "Error clearing Coil memory cache", e)
                }
                MediaUtils.clearRawArtworkCache()
                GlanceShapeBitmaps.clearCache()
                MeloMusicWidget.clearArtCache()
                MeloCookieWidget.clearArtCache()
            }
        }
        
        // Light cleanup when the app moves to the background
        if (level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN || level == ComponentCallbacks2.TRIM_MEMORY_BACKGROUND) {
            Log.d(TAG, "App backgrounded - trimming caches")
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    CacheManager.autoTrimCache(applicationContext, currentMaxCacheSize())
                } catch (e: Exception) {
                    Log.e(TAG, "Error during background cache trim", e)
                }
            }
        }
    }
}
