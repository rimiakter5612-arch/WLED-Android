package ca.cgagnier.wlednativeandroid.service.update

import androidx.compose.runtime.snapshotFlow
import ca.cgagnier.wlednativeandroid.service.websocket.DeviceWithState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DeviceUpdateManager @Inject constructor(
    private val releaseService: ReleaseService
) {

    /**
     * Returns a Flow that emits the version tag (e.g., "v0.14.0") if an update is available,
     * or null if up-to-date.
     */
    fun getUpdateFlow(deviceWithState: DeviceWithState): Flow<String?> {
        return snapshotFlow { deviceWithState.stateInfo.value }
            .map { stateInfo ->
                if (stateInfo == null) return@map null

                val source = UpdateSourceRegistry.getSource(stateInfo.info) ?: return@map null
                releaseService.getNewerReleaseTag(
                    deviceInfo = stateInfo.info,
                    branch = deviceWithState.device.branch,
                    ignoreVersion = deviceWithState.device.skipUpdateTag,
                    updateSourceDefinition = source,
                )
            }
            .distinctUntilChanged()
    }
}