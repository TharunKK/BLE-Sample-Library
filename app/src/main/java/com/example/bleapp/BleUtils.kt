package com.example.bleapp

import android.app.Activity
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.os.ParcelUuid
import android.util.Base64
import android.util.Log
import android.util.SparseArray
import android.webkit.URLUtil
import android.widget.Toast
import java.nio.BufferUnderflowException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID


object BleUtils {

    const val TAG = "THINGY:52"
    val THINGY_BASE_UUID = UUID(-0x1097feff64cab6cdL, -0x64efad00568bffbeL)
    val THINGY_CONFIGURATION_SERVICE = UUID(-0x1097feff64cab6cdL, -0x64efad00568bffbeL)
    val DEVICE_NAME_CHARACTERISTIC_UUID = UUID(-0x1097fefe64cab6cdL, -0x64efad00568bffbeL)
    val ADVERTISING_PARAM_CHARACTERISTIC_UUID = UUID(-0x1097fefd64cab6cdL, -0x64efad00568bffbeL)
    val APPEARANCE_CHARACTERISTIC_UUID = UUID(-0x1097fefc64cab6cdL, -0x64efad00568bffbeL)
    val CONNECTION_PARAM_CHARACTERISTIC_UUID = UUID(-0x1097fefb64cab6cdL, -0x64efad00568bffbeL)
    val EDDYSTONE_URL_CHARACTERISTIC_UUID = UUID(-0x1097fefa64cab6cdL, -0x64efad00568bffbeL)
    val CLOUD_TOKEN_CHARACTERISTIC_UUID = UUID(-0x1097fef964cab6cdL, -0x64efad00568bffbeL)
    val FIRMWARE_VERSION_CHARACTERISTIC_UUID = UUID(-0x1097fef864cab6cdL, -0x64efad00568bffbeL)
    val MTU_CHARACERISTIC_UUID = UUID(-0x1097fef764cab6cdL, -0x64efad00568bffbeL)
    val NFC_CHARACTERISTIC_UUID = UUID(-0x1097fef664cab6cdL, -0x64efad00568bffbeL)
    val BATTERY_SERVICE = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb")
    val BATTERY_SERVICE_CHARACTERISTIC = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb")
    val THINGY_ENVIRONMENTAL_SERVICE = UUID(-0x1097fdff64cab6cdL, -0x64efad00568bffbeL)
    val TEMPERATURE_CHARACTERISTIC = UUID(-0x1097fdfe64cab6cdL, -0x64efad00568bffbeL)
    val PRESSURE_CHARACTERISTIC = UUID(-0x1097fdfd64cab6cdL, -0x64efad00568bffbeL)
    val HUMIDITY_CHARACTERISTIC = UUID(-0x1097fdfc64cab6cdL, -0x64efad00568bffbeL)
    val AIR_QUALITY_CHARACTERISTIC = UUID(-0x1097fdfb64cab6cdL, -0x64efad00568bffbeL)
    val COLOR_CHARACTERISTIC = UUID(-0x1097fdfa64cab6cdL, -0x64efad00568bffbeL)
    val CONFIGURATION_CHARACTERISTIC = UUID(-0x1097fdf964cab6cdL, -0x64efad00568bffbeL)
    val THINGY_UI_SERVICE = UUID(-0x1097fcff64cab6cdL, -0x64efad00568bffbeL)
    val LED_CHARACTERISTIC = UUID(-0x1097fcfe64cab6cdL, -0x64efad00568bffbeL)
    val BUTTON_CHARACTERISTIC = UUID(-0x1097fcfd64cab6cdL, -0x64efad00568bffbeL)
    val THINGY_MOTION_SERVICE = UUID(-0x1097fbff64cab6cdL, -0x64efad00568bffbeL)
    val THINGY_MOTION_CONFIGURATION_CHARACTERISTIC =
        UUID(-0x1097fbfe64cab6cdL, -0x64efad00568bffbeL)
    val TAP_CHARACTERISTIC = UUID(-0x1097fbfd64cab6cdL, -0x64efad00568bffbeL)
    val ORIENTATION_CHARACTERISTIC = UUID(-0x1097fbfc64cab6cdL, -0x64efad00568bffbeL)
    val QUATERNION_CHARACTERISTIC = UUID(-0x1097fbfb64cab6cdL, -0x64efad00568bffbeL)
    val PEDOMETER_CHARACTERISTIC = UUID(-0x1097fbfa64cab6cdL, -0x64efad00568bffbeL)
    val RAW_DATA_CHARACTERISTIC = UUID(-0x1097fbf964cab6cdL, -0x64efad00568bffbeL)
    val EULER_CHARACTERISTIC = UUID(-0x1097fbf864cab6cdL, -0x64efad00568bffbeL)
    val ROTATION_MATRIX_CHARACTERISTIC = UUID(-0x1097fbf764cab6cdL, -0x64efad00568bffbeL)
    val HEADING_CHARACTERISTIC = UUID(-0x1097fbf664cab6cdL, -0x64efad00568bffbeL)
    val GRAVITY_VECTOR_CHARACTERISTIC = UUID(-0x1097fbf564cab6cdL, -0x64efad00568bffbeL)
    val THINGY_SOUND_SERVICE = UUID(-0x1097faff64cab6cdL, -0x64efad00568bffbeL)
    val THINGY_SOUND_CONFIG_CHARACTERISTIC = UUID(-0x1097fafe64cab6cdL, -0x64efad00568bffbeL)
    val THINGY_SPEAKER_DATA_CHARACTERISTIC = UUID(-0x1097fafd64cab6cdL, -0x64efad00568bffbeL)
    val THINGY_SPEAKER_STATUS_CHARACTERISTIC = UUID(-0x1097fafc64cab6cdL, -0x64efad00568bffbeL)
    val THINGY_MICROPHONE_CHARACTERISTIC = UUID(-0x1097fafb64cab6cdL, -0x64efad00568bffbeL)
    val CLIENT_CHARACTERISTIC_CONFIGURATOIN_DESCRIPTOR =
        UUID(0x0000290200001000L, -0x7fffff7fa064cb05L)
    val PARCEL_SECURE_DFU_SERVICE =
        ParcelUuid.fromString("0000FE59-0000-1000-8000-00805F9B34FB")
    val SECURE_DFU_SERVICE = UUID.fromString("0000FE59-0000-1000-8000-00805F9B34FB")
    val THINGY_BUTTONLESS_DFU_SERVICE = UUID(-0x71bffffe0ceab0a0L, -0x60477c77cf2515b0L)
    val DFU_DEFAULT_CONTROL_POINT_CHARACTERISTIC =
        UUID(-0x7136fffe0ceab0a0L, -0x60477c77cf2515b0L)
    val DFU_CONTROL_POINT_CHARACTERISTIC_WITHOUT_BOND_SHARING =
        UUID(-0x7136fffc0ceab0a0L, -0x60477c77cf2515b0L)

    const val ACTION_DEVICE_CONNECTED = "ACTION_DEVICE_CONNECTED_"
    const val ACTION_DEVICE_DISCONNECTED = "ACTION_DEVICE_DISCONNECTED_"
    const val ACTION_SERVICE_DISCOVERY_COMPLETED = "ACTION_SERVICE_DISCOVERY_COMPLETED_"
    const val ACTION_DATA_RECEIVED = "ACTION_DATA_RECEIVED_"
    const val CONNECTION_STATE = "READING_CONFIGURATION"
    private const val ACTION_ENVIRONMENT_CONFIGRATION_READ_COMPLETE =
        "ACTION_ENVIRONMENT_CONFIGRATION_READ_COMPLETE_"
    const val EXTRA_DATA_TEMPERATURE_INTERVAL = "EXTRA_DATA_TEMPERATURE_INTERVAL"
    const val EXTRA_DATA_PRESSURE_INTERVAL = "EXTRA_DATA_PRESSURE_INTERVAL"
    const val EXTRA_DATA_HUMIDITY_INTERVAL = "EXTRA_DATA_HUMIDITY_INTERVAL"
    const val EXTRA_DATA_GAS_MODE = "EXTRA_DATA_GAS_MODE"
    const val EXTRA_DATA_PRESSURE_MODE = "EXTRA_DATA_PRESSURE_MODE"
    const val ACTION_MOTION_CONFIGRATION_READ = "ACTION_MOTION_CONFIGRATION_READ"
    const val EXTRA_DEVICE = "EXTRA_DEVICE"
    const val EXTRA_DEVICE_NAME = "EXTRA_DEVICE_NAME"
    const val EXTRA_DEVICE_ADDRESS = "EXTRA_DEVICE_ADDRESS"
    const val BATTERY_LEVEL_NOTIFICATION = "BATTERY_LEVEL_NOTIFICATION_"
    const val TEMPERATURE_NOTIFICATION = "TEMPERATURE_NOTIFICATION_"
    const val PRESSURE_NOTIFICATION = "PRESSURE_NOTIFICATION_"
    const val HUMIDITY_NOTIFICATION = "HUMIDITY_NOTIFICATION_"
    const val AIR_QUALITY_NOTIFICATION = "AIR_QUALITY_NOTIFICATION_"
    const val COLOR_NOTIFICATION = "COLOR_NOTIFICATION_"
    const val CONFIGURATION_DATA = "CONFIGURATION_DATA_"
    const val BUTTON_STATE_NOTIFICATION = "BUTTON_STATE_NOTIFICATION_"
    const val TAP_NOTIFICATION = "TAP_NOTIFICATION_"
    const val ORIENTATION_NOTIFICATION = "ORIENTATION_NOTIFICATION_"
    const val QUATERNION_NOTIFICATION = "QUATERNION_NOTIFICATION_"
    const val PEDOMETER_NOTIFICATION = "PEDOMETER_NOTIFICATION_"
    const val RAW_DATA_NOTIFICATION = "RAW_DATA_NOTIFICATION_"
    const val EULER_NOTIFICATION = "EULER_NOTIFICATION_"
    const val ROTATION_MATRIX_NOTIFICATION = "ROTATION_MATRIX_NOTIFICATION_"
    const val HEADING_NOTIFICATION = "HEADING_NOTIFICATION_"
    const val GRAVITY_NOTIFICATION = "GRAVITY_NOTIFICATION_"
    const val SPEAKER_STATUS_NOTIFICATION = "SPEAKER_STATUS_NOTIFICATION"
    const val MICROPHONE_NOTIFICATION = "MICROPHONE_NOTIFICATION"
    const val EXTRA_DATA = "EXTRA_DATA"
    const val EXTRA_DATA_TIME_STAMP = "EXTRA_DATA_TIME_STAMP"
    const val EXTRA_DATA_ECO2 = "EXTRA_DATA_ECO2"
    const val EXTRA_DATA_TVOC = "EXTRA_DATA_TVOC"
    const val EXTRA_DATA_RED = "EXTRA_DATA_RED"
    const val EXTRA_DATA_BLUE = "EXTRA_DATA_BLUE"
    const val EXTRA_DATA_GREEN = "EXTRA_DATA_GREEN"
    const val EXTRA_DATA_CLEAR = "EXTRA_DATA_CLEAR"
    const val EXTRA_DATA_BUTTON = "EXTRA_DATA_BUTTON"
    const val EXTRA_DATA_TAP_COUNT = "EXTRA_DATA_TAP_COUNT"
    const val EXTRA_DATA_TAP_DIRECTION = "EXTRA_DATA_TAP_DIRECTION"
    const val EXTRA_DATA_QUATERNION_W = "EXTRA_DATA_QUATERNION_W"
    const val EXTRA_DATA_QUATERNION_X = "EXTRA_DATA_QUATERNION_X"
    const val EXTRA_DATA_QUATERNION_Y = "EXTRA_DATA_QUATERNION_Y"
    const val EXTRA_DATA_QUATERNION_Z = "EXTRA_QUATERNION_Z"
    const val EXTRA_DATA_STEP_COUNT = "EXTRA_DATA_STEP_COUNT"
    const val EXTRA_DATA_DURATION = "EXTRA_DATA_DURATION"
    const val EXTRA_DATA_ACCELEROMETER_X = "EXTRA_DATA_ACCELEROMETER_X"
    const val EXTRA_DATA_ACCELEROMETER_Y = "EXTRA_DATA_ACCELEROMETER_Y"
    const val EXTRA_DATA_ACCELEROMETER_Z = "EXTRA_DATA_ACCELEROMETER_Z"
    const val EXTRA_DATA_GYROSCOPE_X = "EXTRA_DATA_DATA_GYROSCOPE_X"
    const val EXTRA_DATA_GYROSCOPE_Y = "EXTRA_DATA_GYROSCOPE_Y"
    const val EXTRA_DATA_GYROSCOPE_Z = "EXTRA_DATA_GYROSCOPE_Z"
    const val EXTRA_DATA_PITCH = "EXTRA_DATA_PITCH"
    const val EXTRA_DATA_ROLL = "EXTRA_DATA_ROLL"
    const val EXTRA_DATA_YAW = "EXTRA_DATA_YAW"
    const val EXTRA_DATA_GRAVITY_X = "EXTRA_DATA_GRAVITY_X"
    const val EXTRA_DATA_GRAVITY_Y = "EXTRA_DATA_GRAVITY_Y"
    const val EXTRA_DATA_GRAVITY_Z = "EXTRA_DATA_GRAVITY_Z"
    const val EXTRA_DATA_COMPASS_X = "EXTRA_DATA_COMPASS_X"
    const val EXTRA_DATA_COMPASS_Y = "EXTRA_DATA_COMPASS_Y"
    const val EXTRA_DATA_COMPASS_Z = "EXTRA_DATA_COMPASS_Z"
    const val EXTRA_DATA_ROTATION_MATRIX = "EXTRA_DATA_COMPASS_Z"
    const val EXTRA_DATA_SPEAKER_STATUS_NOTIFICATION = "EXTRA_DATA_SPEAKER_STATUS_NOTIFICATION"
    const val EXTRA_DATA_SPEAKER_MODE = "EXTRA_DATA_SPEAKER_MODE"
    const val EXTRA_DATA_MICROPHONE_NOTIFICATION = "EXTRA_DATA_MICROPHONE_NOTIFICATION"
    const val EXTRA_DATA_PCM = "EXTRA_DATA_PCM"
    const val INITIAL_CONFIG_FROM_ACTIVITY = "INITIAL_CONFIG_FROM_ACTIVITY"
    const val SAMPLE_1 = 0
    const val SAMPLE_2 = 1
    const val SAMPLE_3 = 2
    const val SAMPLE_4 = 3
    const val SAMPLE_5 = 4
    const val SAMPLE_6 = 5
    const val SAMPLE_7 = 6
    const val SAMPLE_8 = 7
    const val SAMPLE_9 = 8
    const val BUTTON_STATE_RELEASED = 0x00
    const val BUTTON_STATE_PRESSED = 0x01
    const val OFF: Byte = 0x00
    const val CONSTANT: Byte = 0x01
    const val BREATHE: Byte = 0x02
    const val ONE_SHOT: Byte = 0x03
    const val LED_RED = 0x01
    const val LED_GREEN = 0x02
    const val LED_YELLOW = 0x03
    const val LED_BLUE = 0x04
    const val LED_PURPLE = 0x05
    const val LED_CYAN = 0x06
    const val LED_WHITE = 0x07
    const val DEFAULT_RED_INTENSITY = 0
    const val DEFAULT_GREEN_INTENSITY = 255
    const val DEFAULT_BLUE_INTENSITY = 255
    const val DEFAULT_RED_CALIBRATION_INTENSITY = 103
    const val DEFAULT_GREEN_CALIBRATION_INTENSITY = 78
    const val DEFAULT_BLUE_CALIBRATION_INTENSITY = 29
    const val DEFAULT_BREATHE_INTERVAL = 3500
    const val DEFAULT_MINIMUM_BREATHE_INTERVAL = 50 //ms
    const val DEFAULT_MAXIMUM_BREATHE_INTERVAL = 10000 //ms
    const val DEFAULT_LED_INTENSITY = 20
    const val DEFAULT_MINIMUM_LED_INTENSITY = 1
    const val DEFAULT_MAXIIMUM_LED_INTENSITY = 100
    val DEFAULT_LED_COLOR: Int = Color.CYAN
    const val TAP_X_UP = 0x01
    const val TAP_X_DOWN = 0x02
    const val TAP_Y_UP = 0x03
    const val TAP_Y_DOWN = 0x04
    const val TAP_Z_UP = 0x05
    const val TAP_Z_DOWN = 0x06
    const val X_UP = "FRONT"
    const val X_DOWN = "BACK"
    const val Y_UP = "RIGHT"
    const val Y_DOWN = "LEFT"
    const val Z_UP = "TOP"
    const val Z_DOWN = "BOTTOM"
    const val PORTRAIT_TYPE = 0x00
    const val LANDSCAPE_TYPE = 0x01
    const val REVERSE_PORTRAIT_TYPE = 0x02
    const val REVERSE_LANDSCAPE_TYPE = 0x03
    const val PORTRAIT = "PORTRAIT"
    const val LANDSCAPE = "LANDSCAPE"
    const val REVERSE_PORTRAIT = "R PORTRAIT"
    const val REVERSE_LANDSCAPE = "R LANDSCAPE"
    const val FORMAT_UINT24 = 0x13
    const val FORMAT_SINT24 = 0x23
    const val FORMAT_UINT16_BIG_INDIAN = 0x62
    const val FORMAT_UINT32_BIG_INDIAN = 0x64
    const val MAX_VISISBLE_GRAPH_ENTRIES = 300
    val TIME_FORMAT: SimpleDateFormat = SimpleDateFormat("HH:mm:ss:SSS", Locale.US)
    val TIME_FORMAT_PEDOMETER: SimpleDateFormat = SimpleDateFormat("mm:ss:SS", Locale.US)
    val GRAVITY_VECTOR_DECIMAL_FORMAT: DecimalFormat = DecimalFormat("#.##")
    const val CLOUD_TOKEN_LENGTH = 250
    var ADVERTISING_INTERVAL_UNIT = 0.625 //ms
    var CONN_INT_UNIT = 1.25
    var MIN_CONN_VALUE = 6
    var MIN_CONN_INTERVAL = 7.5 //ms
    var MAX_CONN_VALUE = 3200
    var MAX_CONN_INTERVAL = 4000 //ms
    var MIN_SLAVE_LATENCY = 0
    var MAX_SLAVE_LATENCY = 500
    var MIN_SUPERVISION_TIMEOUT = 100 //ms
    var MAX_SUPERVISION_TIMEOUT = 32000 //ms

    //Android BLE
    var MAX_CONN_INTERVAL_POST_LOLIPOP = 11.25 //ms
    const val MAX_MTU_SIZE_THINGY = 276
    const val MAX_MTU_SIZE_PRE_LOLLIPOP = 23
    const val MAX_AUDIO_PACKET_SIZE = 160 //

    //Notification Intervals in ms
    const val ENVIRONMENT_NOTIFICATION_MAX_INTERVAL = 60000
    const val NOTIFICATION_MAX_INTERVAL = 5000
    const val TEMP_MIN_INTERVAL = 100
    const val PRESSURE_MIN_INTERVAL = 50
    const val HUMIDITY_MIN_INTERVAL = 100
    const val COLOR_INTENSITY_MIN_INTERVAL = 200
    const val PEDOMETER_MIN_INTERVAL = 100
    const val COMPASS_MIN_INTERVAL = 100
    const val MPU_FREQ_MIN_INTERVAL = 5 //hz
    const val MPU_FREQ_MAX_INTERVAL = 200 //hz
    const val GAS_MODE_1 = 1
    const val GAS_MODE_2 = 2
    const val GAS_MODE_3 = 3
    const val WAKE_ON_MOTION_ON = 0x00
    const val WAKE_ON_MOTION_OFF = 0x01

    //Speaker modes
    const val FREQUENCY_MODE = 0x01
    const val PCM_MODE = 0x02
    const val SAMPLE_MODE = 0x03

    //Microphone mode
    const val ADPCM_MODE = 0x01
    const val SPL_MODE = 0x02

    //Speaker status notifications
    const val SPEAKER_STATUS_FINISHED = 0x00
    const val SPEAKER_STATUS_BUFFER_WARNING = 0x01
    const val SPEAKER_STATUS_BUFFER_READY = 0x02
    const val SPEAKER_STATUS_PACKET_DISREGARDED = 0x10
    const val SPEAKER_STATUS_INVALID_COMMAND = 0x11

    /**
     * URI Scheme maps a byte code into the scheme and an optional scheme specific prefix.
     */
    private val URI_SCHEMES: SparseArray<String?> = object : SparseArray<String?>() {
        init {
            put(0.toByte().toInt(), "http://www.")
            put(1.toByte().toInt(), "https://www.")
            put(2.toByte().toInt(), "http://")
            put(3.toByte().toInt(), "https://")
            put(4.toByte().toInt(), "urn:uuid:") // RFC 2141 and RFC 4122};
        }
    }

    /**
     * Expansion strings for "http" and "https" schemes. These contain strings appearing anywhere in a
     * URL. Restricted to Generic TLDs.
     *
     *
     * Note: this is a scheme specific encoding.
     */
    private val URL_CODES: SparseArray<String?> = object : SparseArray<String?>() {
        init {
            put(0.toByte().toInt(), ".com/")
            put(1.toByte().toInt(), ".org/")
            put(2.toByte().toInt(), ".edu/")
            put(3.toByte().toInt(), ".net/")
            put(4.toByte().toInt(), ".info/")
            put(5.toByte().toInt(), ".biz/")
            put(6.toByte().toInt(), ".gov/")
            put(7.toByte().toInt(), ".com")
            put(8.toByte().toInt(), ".org")
            put(9.toByte().toInt(), ".edu")
            put(10.toByte().toInt(), ".net")
            put(11.toByte().toInt(), ".info")
            put(12.toByte().toInt(), ".biz")
            put(13.toByte().toInt(), ".gov")
        }
    }

    fun createSpeakerStatusChangeReceiver(address: String?): IntentFilter {
        val intentFilter = IntentFilter()
        intentFilter.addAction(ACTION_SERVICE_DISCOVERY_COMPLETED)
        intentFilter.addAction(ACTION_DEVICE_DISCONNECTED)
        intentFilter.addAction(EXTRA_DATA_SPEAKER_STATUS_NOTIFICATION)
        return intentFilter
    }

    fun setValue(dest: ByteArray, offset: Int, value: Int, formatType: Int): Int {
        var offset = offset
        var value = value
        val len = offset + getTypeLen(formatType)
        if (len > dest.size) return offset
        when (formatType) {
            BluetoothGattCharacteristic.FORMAT_SINT8 -> {
                value = intToSignedBits(value, 8)
                dest[offset] = (value and 0xFF).toByte()
            }

            BluetoothGattCharacteristic.FORMAT_UINT8 -> dest[offset] = (value and 0xFF).toByte()
            BluetoothGattCharacteristic.FORMAT_SINT16 -> {
                value = intToSignedBits(value, 16)
                dest[offset++] = (value and 0xFF).toByte()
                dest[offset] = (value shr 8 and 0xFF).toByte()
            }

            BluetoothGattCharacteristic.FORMAT_UINT16 -> {
                dest[offset++] = (value and 0xFF).toByte()
                dest[offset] = (value shr 8 and 0xFF).toByte()
            }

            FORMAT_SINT24 -> {
                value = intToSignedBits(value, 24)
                dest[offset++] = (value and 0xFF).toByte()
                dest[offset++] = (value shr 8 and 0xFF).toByte()
                dest[offset] = (value shr 16 and 0xFF).toByte()
            }

            FORMAT_UINT24 -> {
                dest[offset++] = (value and 0xFF).toByte()
                dest[offset++] = (value shr 8 and 0xFF).toByte()
                dest[offset] = (value shr 16 and 0xFF).toByte()
            }

            FORMAT_UINT16_BIG_INDIAN -> {
                dest[offset++] = (value shr 8 and 0xFF).toByte()
                dest[offset] = (value and 0xFF).toByte()
            }

            BluetoothGattCharacteristic.FORMAT_SINT32 -> {
                value = intToSignedBits(value, 32)
                dest[offset++] = (value and 0xFF).toByte()
                dest[offset++] = (value shr 8 and 0xFF).toByte()
                dest[offset++] = (value shr 16 and 0xFF).toByte()
                dest[offset] = (value shr 24 and 0xFF).toByte()
            }

            BluetoothGattCharacteristic.FORMAT_UINT32 -> {
                dest[offset++] = (value and 0xFF).toByte()
                dest[offset++] = (value shr 8 and 0xFF).toByte()
                dest[offset++] = (value shr 16 and 0xFF).toByte()
                dest[offset] = (value shr 24 and 0xFF).toByte()
            }

            FORMAT_UINT32_BIG_INDIAN -> {
                dest[offset++] = (value shr 24 and 0xFF).toByte()
                dest[offset++] = (value shr 16 and 0xFF).toByte()
                dest[offset++] = (value shr 8 and 0xFF).toByte()
                dest[offset] = (value and 0xFF).toByte()
            }

            else -> return offset
        }
        return len
    }

    private fun getTypeLen(formatType: Int): Int {
        return formatType and 0xF
    }

    private fun intToSignedBits(i: Int, size: Int): Int {
        var i = i
        if (i < 0) {
            i = 1 shl size - 1 + (i and (1 shl size) - 1 - 1)
        }
        return i
    }

    fun showToast(activity: Activity?, message: String?) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    fun showToast(context: Context?, message: String?) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun decodeUri(serviceData: ByteArray, start: Int, length: Int): String? {
        if (start < 0 || serviceData.size < start + length) return null
        val uriBuilder = StringBuilder()
        var offset = 0
        if (offset < length) {
            val b = serviceData[start + offset++]
            val scheme = URI_SCHEMES[b.toInt()]
            if (scheme != null) {
                uriBuilder.append(scheme)
                if (URLUtil.isNetworkUrl(scheme)) {
                    return decodeUrl(serviceData, start + offset, length - 1, uriBuilder)
                } else if ("urn:uuid:" == scheme) {
                    return decodeUrnUuid(serviceData, start + offset, uriBuilder)
                }
            }
            Log.w(TAG, "decodeUri unknown Uri scheme code=$b")
        }
        return null
    }

    private fun decodeUrl(
        serviceData: ByteArray,
        start: Int,
        length: Int,
        urlBuilder: StringBuilder
    ): String {
        var offset = 0
        while (offset < length) {
            val b = serviceData[start + offset++]
            val code = URL_CODES[b.toInt()]
            if (code != null) {
                urlBuilder.append(code)
            } else {
                urlBuilder.append(Char(b.toUShort()))
            }
        }
        return urlBuilder.toString()
    }

    /**
     * Creates the Uri string with embedded expansion codes.
     *
     * @param uri to be encoded
     * @return the Uri string with expansion codes.
     */
    fun encodeUri(uri: String): ByteArray? {
        if (uri.length == 0) {
            return ByteArray(0)
        }
        val bb = ByteBuffer.allocate(uri.length)
        // UUIDs are ordered as byte array, which means most significant first
        bb.order(ByteOrder.BIG_ENDIAN)
        var position = 0

        // Add the byte code for the scheme or return null if none
        val schemeCode = encodeUriScheme(uri) ?: return null
        val scheme = URI_SCHEMES[schemeCode.toInt()]
        bb.put(schemeCode)
        position += scheme!!.length
        if (URLUtil.isNetworkUrl(scheme)) {
            return encodeUrl(uri, position, bb)
        } else if ("urn:uuid:" == scheme) {
            return encodeUrnUuid(uri, position, bb)
        }
        return null
    }

    private fun encodeUriScheme(uri: String): Byte? {
        val lowerCaseUri = uri.lowercase(Locale.ENGLISH)
        for (i in 0 until URI_SCHEMES.size()) {
            // get the key and value.
            val key = URI_SCHEMES.keyAt(i)
            val value = URI_SCHEMES.valueAt(i)
            if (lowerCaseUri.startsWith(value!!)) {
                return key.toByte()
            }
        }
        return null
    }

    private fun encodeUrl(url: String, position: Int, bb: ByteBuffer): ByteArray {
        var position = position
        while (position < url.length) {
            val expansion = findLongestExpansion(url, position)
            if (expansion >= 0) {
                bb.put(expansion)
                position += URL_CODES[expansion.toInt()]!!.length
            } else {
                bb.put(url[position++].code.toByte())
            }
        }
        return byteBufferToArray(bb)
    }

    private fun byteBufferToArray(bb: ByteBuffer): ByteArray {
        val bytes = ByteArray(bb.position())
        bb.rewind()
        bb[bytes, 0, bytes.size]
        return bytes
    }

    /**
     * Finds the longest expansion from the uri at the current position.
     *
     * @param uriString the Uri
     * @param pos start position
     * @return an index in URI_MAP or 0 if none.
     */
    private fun findLongestExpansion(uriString: String, pos: Int): Byte {
        var expansion: Byte = -1
        var expansionLength = 0
        for (i in 0 until URL_CODES.size()) {
            // get the key and value.
            val key = URL_CODES.keyAt(i)
            val value = URL_CODES.valueAt(i)
            if (value!!.length > expansionLength && uriString.startsWith(value!!, pos)) {
                expansion = key.toByte()
                expansionLength = value.length
            }
        }
        return expansion
    }

    private fun encodeUrnUuid(urn: String, position: Int, bb: ByteBuffer): ByteArray? {
        val uuidString = urn.substring(position, urn.length)
        val uuid: UUID
        uuid = try {
            UUID.fromString(uuidString)
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "encodeUrnUuid invalid urn:uuid format - $urn")
            return null
        }
        // UUIDs are ordered as byte array, which means most significant first
        bb.order(ByteOrder.BIG_ENDIAN)
        bb.putLong(uuid.mostSignificantBits)
        bb.putLong(uuid.leastSignificantBits)
        return byteBufferToArray(bb)
    }

    private fun decodeUrnUuid(
        serviceData: ByteArray,
        offset: Int,
        urnBuilder: StringBuilder
    ): String? {
        val bb = ByteBuffer.wrap(serviceData)
        // UUIDs are ordered as byte array, which means most significant first
        bb.order(ByteOrder.BIG_ENDIAN)
        val mostSignificantBytes: Long
        val leastSignificantBytes: Long
        try {
            bb.position(offset)
            mostSignificantBytes = bb.long
            leastSignificantBytes = bb.long
        } catch (e: BufferUnderflowException) {
            Log.w(TAG, "decodeUrnUuid BufferUnderflowException!")
            return null
        }
        val uuid = UUID(mostSignificantBytes, leastSignificantBytes)
        urnBuilder.append(uuid.toString())
        return urnBuilder.toString()
    }

    /**
     * Convert a signed byte to an unsigned int.
     */
    private fun unsignedByteToInt(b: Byte): Int {
        return b.toInt() and 0xFF
    }

    /**
     * Convert signed bytes to a 16-bit unsigned int.
     */
    private fun unsignedBytesToInt(b0: Byte, b1: Byte): Int {
        return unsignedByteToInt(b0) + (unsignedByteToInt(b1) shl 8)
    }

    /**
     * Convert an unsigned integer value to a two's-complement encoded signed value.
     */
    private fun unsignedToSigned(unsigned: Int, size: Int): Int {
        var unsigned = unsigned
        if (unsigned and (1 shl size) - 1 != 0) {
            unsigned = -1 * (1 shl size - 1 - (unsigned and (1 shl size) - 1 - 1))
        }
        return unsigned
    }

    fun base64Decode(s: String?): ByteArray {
        return Base64.decode(s, Base64.DEFAULT)
    }

    fun checkIfVersionIsLollipopOrAbove(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
    }

    fun checkIfVersionIsOreoOrAbove(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    }

    fun validateSlaveLatency(
        slaveLatency: Int,
        maxConIntervalUnits: Int,
        supervisionTimeoutUnits: Int
    ): Boolean {
        val maxConInterval = maxConIntervalUnits.toDouble()
        return if (slaveLatency < supervisionTimeoutUnits * 4 / maxConInterval - 1) {
            true
        } else false
    }

    fun validateSupervisionTimeout(
        slaveLatency: Int,
        maxConIntervalUnits: Int,
        supervisionTimeoutUnits: Int
    ): Boolean {
        val maxConInterval = maxConIntervalUnits.toDouble()
        return if (supervisionTimeoutUnits > (1 + slaveLatency) * maxConInterval / 4) {
            true
        } else false
    }

    fun validateMaxConnectionInterval(
        slaveLatency: Int,
        maxConIntervalUnits: Int,
        supervisionTimeoutUnits: Int
    ): Boolean {
        val maxConInterval = maxConIntervalUnits.toDouble()
        return if (maxConInterval < supervisionTimeoutUnits * 4 / (1 + slaveLatency)) {
            true
        } else false
    }

    fun removeOldDataForGraphs(linkedHashMap: LinkedHashMap<*, *>) {
        if (linkedHashMap.size > MAX_VISISBLE_GRAPH_ENTRIES) {
            val keys: Set<*> = linkedHashMap.keys
            for (key in keys) {
                linkedHashMap.remove(key)
                if (linkedHashMap.size == MAX_VISISBLE_GRAPH_ENTRIES) {
                    break
                }
            }
        }
    }
}