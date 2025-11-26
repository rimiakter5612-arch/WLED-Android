package ca.cgagnier.wlednativeandroid.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

/**
 * Represents a stateless WLED device
 */
@Entity(tableName = "device2")
@Parcelize
data class Device(
    @PrimaryKey
    val macAddress: String,

    val address: String,

    val isHidden: Boolean = false,

    @ColumnInfo(defaultValue = "")
    val originalName: String = "",

    @ColumnInfo(defaultValue = "")
    val customName: String = "",

    @ColumnInfo(defaultValue = "")
    val skipUpdateTag: String = "",

    @ColumnInfo(defaultValue = "UNKNOWN")
    val branch: Branch = Branch.UNKNOWN,

    @ColumnInfo(defaultValue = "UNKNOWN")
    val lastSeen: Long = System.currentTimeMillis(),
) : Parcelable {

    fun getDeviceUrl(): String {
        return "http://$address"
    }
}