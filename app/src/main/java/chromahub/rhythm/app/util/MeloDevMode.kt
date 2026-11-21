/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Melo Dev Mode — identifies the creator's build.
 */

package chromahub.rhythm.app.util

import com.melo.app.BuildConfig
import com.melo.app.R

/** Creator identity shown only in builds compiled with -PdevBuild=true. */
object MeloDevMode {
    val enabled: Boolean get() = BuildConfig.IS_DEV_BUILD
    const val NAME = "Angel Dev"
    const val AVATAR_RES = R.drawable.dev_angel_avatar
    const val CROWN_RES = R.drawable.ic_dev_crown
}
