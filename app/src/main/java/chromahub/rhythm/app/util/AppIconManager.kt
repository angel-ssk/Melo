package chromahub.rhythm.app.util

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

/** Switches the launcher icon by enabling one MAIN/LAUNCHER activity-alias. */
object AppIconManager {
    data class Style(val key: String, val alias: String, val label: String, val color: Long)

    val STYLES = listOf(
        Style("original", "MainActivity_Original", "Melo", 0xFF1946B5),
        Style("azure", "MainActivity_Azure", "Azure", 0xFF1946B5),
        Style("ocean", "MainActivity_Ocean", "Ocean", 0xFF0B6E8F),
        Style("forest", "MainActivity_Forest", "Forest", 0xFF1E7A4E),
        Style("crimson", "MainActivity_Crimson", "Crimson", 0xFFA8283A),
        Style("mono", "MainActivity_Mono", "Mono", 0xFF1B1B1F),
        Style("coral", "MainActivity_Coral", "Coral", 0xFFE2664A),
    )

    fun apply(context: Context, key: String) {
        val pm = context.packageManager
        STYLES.forEach { style ->
            val enabled = style.key == key
            pm.setComponentEnabledSetting(
                ComponentName(context.packageName, "chromahub.rhythm.app.activities.${style.alias}"),
                if (enabled) PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
        }
    }
}
