package ca.cgagnier.wlednativeandroid.ui.homeScreen.deviceAdd

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.cgagnier.wlednativeandroid.R
import ca.cgagnier.wlednativeandroid.domain.use_case.ValidateAddress
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.repository.DeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeviceAddViewModel @Inject constructor(
    private val repository: DeviceRepository, private val validateAddress: ValidateAddress
) : ViewModel() {

    var state by mutableStateOf(DeviceAddState())
    private var findDeviceJob: Job? = null

    fun setAddress(address: String) {
        if (state.step is DeviceAddStep.Form) state = state.copy(address = address)
    }

    fun submitCreateDevice() {
        findDeviceJob = viewModelScope.launch(Dispatchers.IO) {
            val emailResult = validateAddress.execute(state.address)
            val hasError = listOf(
                emailResult
            ).any { !it.successful }
            if (hasError) {
                state = state.copy(
                    step = DeviceAddStep.Form(addressError = emailResult.errorMessage)
                )
                return@launch
            }

            findDevice()
        }
    }

    /**
     * Starts searching for the device and adds it, if one is found
     */
    private suspend fun findDevice() {
        state = state.copy(step = DeviceAddStep.Adding)

        // TODO: Launch device connection here or something
        delay(5000)

        // If the dialog was closed before we got here, don't update the state
        if (!currentCoroutineContext().isActive) {
            return
        }
        // TODO: Add error check here
        if (false) {
            state = state.copy(
                step = DeviceAddStep.Form(
                    // TODO: Put correct error messages
                    addressError = R.string.leave_this_empty_to_use_the_device_name
                )
            )
            return
        }
        state = state.copy(
            step = DeviceAddStep.Success(
                device = Device(
                    state.address,
                    state.address,
                    originalName = state.address
                )
            )
        )

    }

    /**
     * Clears the current state and cancels the find device job
     */
    fun clear() {
        // This needs to be canceled to avoid showing a "success" screen if the user
        // reopens the "add device" screen quickly after dismissing it in the "Adding" step.
        findDeviceJob?.cancel()
        state = DeviceAddState()
    }
}