/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Melo Dev Mode — creator Easter Egg.
 */

package chromahub.rhythm.app.util

import com.melo.app.BuildConfig
import com.melo.app.R

/**
 * Creator identity. Activates as an Easter Egg when the user types the exact
 * name "Angel Dev" in their profile, or unconditionally in builds compiled
 * with -PdevBuild=true.
 */
object MeloDevMode {
    const val NAME = "Angel Dev"
    // Must be getters, not const val: const values are inlined as raw ints,
    // so the resource shrinker cannot see the R reference and strips the
    // drawables, leaving id 0x0 at runtime.
    val AVATAR_RES: Int get() = R.drawable.dev_angel_avatar
    val CROWN_RES: Int get() = R.drawable.ic_dev_crown

    fun isDevName(name: String?): Boolean =
        name?.trim()?.equals(NAME, ignoreCase = true) == true

    val enabledByBuild: Boolean get() = BuildConfig.IS_DEV_BUILD

    fun isEnabled(currentName: String?): Boolean =
        enabledByBuild || isDevName(currentName)
}
