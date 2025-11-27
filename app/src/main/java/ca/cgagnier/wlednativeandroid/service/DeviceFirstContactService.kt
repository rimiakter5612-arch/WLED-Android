package ca.cgagnier.wlednativeandroid.service

import android.util.Log
import androidx.lifecycle.ViewModel
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.model.wledapi.Info
import ca.cgagnier.wlednativeandroid.repository.DeviceRepository
import ca.cgagnier.wlednativeandroid.service.api.DeviceApi
import ca.cgagnier.wlednativeandroid.service.websocket.DeviceWithState
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val TAG = "DeviceFirstContactService"

/**
 * Service class responsible for handling the first contact with a device.
 */
class DeviceFirstContactService @Inject constructor(
    private val repository: DeviceRepository,
) {
    /**
     * Creates a new device record in the database.
     * Assumes the device does not already exist.
     * @param macAddress - The unique MAC address for the new device.
     * @param address - The network address (e.g., IP) for the new device.
     * @param name - The name of the new device.
     * @return The newly created device object.
     */
    private suspend fun createDevice(
        macAddress: String, address: String, name: String
    ): Device {
        Log.d(TAG, "Creating new device entry for MAC: $macAddress at address: $address")
        val device = Device(
            macAddress = macAddress,
            address = address,
            originalName = name,
        )
        repository.insert(device)
        return device
    }

    /**
     * Updates the address of an existing device record in the database.
     * @param device - The existing device object to update.
     * @param newAddress - The new network address for the device.
     * @param name - The new name of the device.
     * @return The updated device object.
     */
    private suspend fun updateDeviceAddress(
        device: Device, newAddress: String, name: String
    ): Device {
        Log.d(TAG, "Updating address for device MAC: ${device.macAddress} to: $newAddress")
        val updatedDevice = device.copy(address = newAddress, originalName = name)
        repository.update(updatedDevice)
        return updatedDevice
    }

    /**
     * Fetches device information from the specified address.
     * @param address - The network address (e.g., IP) to query.
     * @return The device information object.
     */
    private suspend fun getDeviceInfo(address: String): Info {
        val timeout = 10L // seconds

        // Normalize the address to ensure it's a valid base URL
        val baseUrl = if (!address.startsWith("http://") && !address.startsWith("https://")) {
            "http://$address/"
        } else {
            address
        }

        val okHttpClient = OkHttpClient().newBuilder().connectTimeout(timeout, TimeUnit.SECONDS)
            .readTimeout(timeout, TimeUnit.SECONDS).writeTimeout(timeout, TimeUnit.SECONDS).build()
        val deviceApi = Retrofit.Builder().baseUrl(baseUrl).client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create()).build()
            .create(DeviceApi::class.java)

        return deviceApi.getInfo().body()!!
    }

    /**
     * Fetches device information using its address, then ensures a corresponding
     * device record exists in the database (creating or updating its address
     * as necessary). Returns the device.
     *
     * @param address - The network address (e.g., IP) to query.
     * @return The device object.
     * @throws Exception if device info cannot be fetched or lacks a MAC address.
     */
    suspend fun fetchAndUpsertDevice(address: String): Device {
        Log.d(TAG, "Trying to create a new device: $address")
        val info = getDeviceInfo(address)

        if (info.macAddress.isNullOrEmpty()) {
            Log.e(TAG, "Could not retrieve MAC address for device at ${address}. Response: $info")
            throw Exception("Could not retrieve MAC address for device at $address")
        }

        val existingDevice = repository.findDeviceByMacAddress(info.macAddress)

        if (existingDevice == null) {
            Log.d(TAG, "No existing device found for MAC: ${info.macAddress}. Creating new entry.")
            return createDevice(info.macAddress, address, info.name)
        }
        Log.d(TAG, "Device found for MAC: ${info.macAddress}")
        if (existingDevice.address == address && existingDevice.originalName == info.name) {
            return existingDevice
        }
        Log.d(
            TAG, "Updating address for device MAC: ${existingDevice.macAddress} to: $address"
        )
        return updateDeviceAddress(existingDevice, address, info.name)
    }
}