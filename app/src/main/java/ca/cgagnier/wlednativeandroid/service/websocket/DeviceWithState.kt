package ca.cgagnier.wlednativeandroid.service.websocket

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.model.wledapi.DeviceStateInfo
import ca.cgagnier.wlednativeandroid.service.update.DeviceUpdateManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

const val AP_MODE_MAC_ADDRESS = "AP-MODE"

class DeviceWithState(
    initialDevice: Device,
    deviceUpdateManager: DeviceUpdateManager? = null
) {
    var device: Device by mutableStateOf(initialDevice)
    val stateInfo: MutableState<DeviceStateInfo?> = mutableStateOf(null)
    val isWebsocketConnected: MutableState<Boolean> = mutableStateOf(false)
    // TODO: Add websocket connection status, like offline/online/connecting

    val updateVersionTagFlow: Flow<String?> =
        deviceUpdateManager?.getUpdateFlow(this) ?: flowOf(null)

    fun isAPMode(): Boolean {
        return device.macAddress == AP_MODE_MAC_ADDRESS
    }
}

/**
 * Get a DeviceWithState that can be used to represent a temporary WLED device in AP mode.
 */
fun getApModeDeviceWithState(): DeviceWithState {
    val device = DeviceWithState(
        Device(
            macAddress = AP_MODE_MAC_ADDRESS,
            address = "4.3.2.1",
        )
    )
    device.isWebsocketConnected.value = true

    return device
}