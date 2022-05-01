package io.radar.sdk

import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanRecord
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import io.radar.sdk.model.RadarBeacon
import org.json.JSONObject
import java.nio.ByteBuffer
import java.util.*

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
internal object RadarBeaconUtils {

    private const val MANUFACTURER_ID = 76

    fun getScanFilter(beacon: RadarBeacon): ScanFilter? {
        val uuid = UUID.fromString(beacon.uuid)
        val major = beacon.major.toInt()
        val minor = beacon.minor.toInt()

        val manufacturerData = ByteBuffer.allocate(23)
            .put(ByteArray(2) { 0x00.toByte() })
            .putLong(uuid.mostSignificantBits)
            .putLong(uuid.leastSignificantBits)
            .put((major / 256).toByte())
            .put((major % 256).toByte())
            .put((minor / 256).toByte())
            .put((minor % 256).toByte())
            .put(ByteArray(1) { 0x00.toByte() })
            .array()

        val manufacturerDataMask = ByteBuffer.allocate(23)
            .put(ByteArray(2) { 0x00.toByte() })
            .put(ByteArray(20) { 0xFF.toByte() })
            .put(ByteArray(1) { 0x00.toByte() })
            .array()

        return ScanFilter.Builder()
            .setManufacturerData(MANUFACTURER_ID, manufacturerData, manufacturerDataMask)
            .build()
    }

    // TODO how to scan based on UUID only?
    fun getScanFilter(beaconUUID: String): ScanFilter? {
        val uuid = UUID.fromString(beaconUUID)

        val manufacturerData = ByteBuffer.allocate(23)
            .put(ByteArray(2) { 0x00.toByte() })
            .putLong(uuid.mostSignificantBits)
            .putLong(uuid.leastSignificantBits)
            .put(ByteArray(9) { 0x00.toByte() })
            .array()

        val manufacturerDataMask = ByteBuffer.allocate(23)
            .put(ByteArray(2) { 0x00.toByte() })
            .put(ByteArray(20) { 0xFF.toByte() })
            .put(ByteArray(1) { 0x00.toByte() })
            .array()

        return ScanFilter.Builder()
            .setManufacturerData(MANUFACTURER_ID, manufacturerData, manufacturerDataMask)
            .build()
    }

    fun getBeacon(scanRecord: ScanRecord): JSONObject? {
        val bytes = scanRecord.bytes

        var startByte = 2
        var iBeacon = false
        while (startByte <= 5) {
            if ((bytes[startByte + 2].toInt() and 0xFF) == 0x02 &&
                (bytes[startByte + 3].toInt() and 0xFF) == 0x15) {
                iBeacon = true
                break
            }
            startByte++
        }

        if (!iBeacon) {
            return null
        }

        val buf = ByteBuffer.wrap(bytes, startByte + 4, 20)
        val uuid = UUID(buf.long, buf.long)
        val major = ((buf.get().toInt() and 0xFF) * 0x100 + (buf.get().toInt() and 0xFF)).toString()
        val minor = ((buf.get().toInt() and 0xFF) * 0x100 + (buf.get().toInt() and 0xFF)).toString()

        val beacon = JSONObject()
        beacon.put("uuid", uuid.toString())
        beacon.put("major", major)
        beacon.put("minor", minor)

        return beacon
    }

}