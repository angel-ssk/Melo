/*
 * SPDX-FileCopyrightText: 2024-2026 Anjishnu Nandi <https://github.com/angel-ssk>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.melo.player.shared.presentation.components.common

import com.melo.player.shared.data.model.UserAudioDevice
import com.melo.player.shared.presentation.components.icons.MaterialSymbolIcon
import com.melo.player.shared.presentation.components.icons.MeloIcons

/**
 * Returns the appropriate MaterialSymbolIcon based on the detected or selected DeviceType.
 */
fun getDeviceIcon(type: UserAudioDevice.DeviceType): MaterialSymbolIcon {
    return when (type) {
        UserAudioDevice.DeviceType.HEADPHONES -> MeloIcons.Headphones
        UserAudioDevice.DeviceType.EARBUDS -> MaterialSymbolIcon("earbuds")
        UserAudioDevice.DeviceType.IEM -> MaterialSymbolIcon("earbuds")
        UserAudioDevice.DeviceType.SPEAKERS -> MeloIcons.Speaker
        UserAudioDevice.DeviceType.BLUETOOTH_SPEAKER -> MeloIcons.BluetoothFilled
        UserAudioDevice.DeviceType.CAR_AUDIO -> MaterialSymbolIcon("directions_car")
        UserAudioDevice.DeviceType.STUDIO_MONITORS -> MaterialSymbolIcon("speaker_group")
        UserAudioDevice.DeviceType.OTHER -> MeloIcons.Headphones
    }
}
