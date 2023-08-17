package com.example.bleapp

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.ParcelUuid
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bleapp.BluetoothLeService.Companion.ACTION_GATT_SERVICES_DISCOVERED
import com.example.bleapp.Utils.LED_BLUE
import com.example.bleapp.Utils.LED_RED
import com.example.bleapp.Utils.THINGY_UI_SERVICE
import com.example.blesamplelibrary.BleActivity


private const val ENABLE_BLUETOOTH_REQUEST_CODE = 1
private const val RUNTIME_PERMISSION_REQUEST_CODE = 2

class MainActivity : AppCompatActivity(), ScanAdapter.ScanListener {

    private lateinit var btStartScan: Button
    private lateinit var btStopScan: Button
    private lateinit var btRed: Button
    private lateinit var btBlue: Button
    private lateinit var tvStatus: TextView
    private lateinit var scan_results_recycler_view: RecyclerView

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothManager: BluetoothManager

    private lateinit var bluetoothLeScanner: BluetoothLeScanner
    private var bluetoothService: BluetoothLeService? = null
    private var scanning = false
    private val handler = Handler()

    // Stops scanning after 10 seconds.
    private val SCAN_PERIOD: Long = 10000
    var bluetoothGatt: BluetoothGatt? = null
    var deviceAddress = ""

    // Device scan callback.
    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            if (!scanResults.contains(result)) {
                with(result.device) {
                    Log.i(
                        "ScanCallback",
                        "Found BLE device! Name: ${name ?: "Unnamed"}, address: $address"
                    )
                }
                scanResults.add(result)
            }
            setupRecyclerView(scanResults)
        }
    }

    private var scanResults = mutableListOf<ScanResult>()

    lateinit var nameChar: BluetoothGattCharacteristic
    lateinit var batteryChar: BluetoothGattCharacteristic
    lateinit var ledChar: BluetoothGattCharacteristic
    lateinit var buttonChar: BluetoothGattCharacteristic

    var buttonState = 0

    var requestedCharacteristic: ArrayList<BluetoothGattCharacteristic> = ArrayList()

    private var ledMode: Int = Utils.BREATHE.toInt()
    private var ledColorIndex: Int = Utils.BREATHE.toInt()
    private var ledColorIntensity: Int = Utils.BREATHE.toInt()
    private var ledBreatheDelay: Int = Utils.BREATHE.toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btStartScan = findViewById(R.id.btStartScan)
        btStopScan = findViewById(R.id.btStopScan)
        btRed = findViewById(R.id.btRed)
        btBlue = findViewById(R.id.btBlue)
        tvStatus = findViewById(R.id.tvStatus)
        scan_results_recycler_view = findViewById(R.id.scan_results_recycler_view)

        bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner

        btStartScan.setOnClickListener {
            scanResults = mutableListOf()

            if (bluetoothAdapter == null) {
                // Device doesn't support Bluetooth
            } else if (bluetoothAdapter?.isEnabled == false) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestBluetoothPermissions()
                } else if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestLocationPermission()
                } else {
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    startActivity(enableBtIntent)
                }
            } else {
                scanLeDevice()
            }
        }

        btStopScan.setOnClickListener {
            if (bluetoothGatt != null) {
                btStopScan.text = "Stop Scan"
                scanResults = mutableListOf()
                setupRecyclerView(scanResults)

                bluetoothGatt!!.disconnect()
                bluetoothGatt!!.close()
                bluetoothGatt = null
                requestedCharacteristic = ArrayList()
            }
        }

        btRed.setOnClickListener {
            if (bluetoothGatt != null) {
                val colorData = ByteArray(5)
                colorData[0] = Utils.BREATHE
                colorData[1] = LED_RED.toByte()
                colorData[2] = 50.toByte()
                Utils.setValue(
                    colorData,
                    3,
                    ledBreatheDelay,
                    BluetoothGattCharacteristic.FORMAT_UINT16
                )
                ledChar.value = colorData
                ledChar.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                val isRedChanged = bluetoothGatt!!.writeCharacteristic(ledChar)
                /*val isRedChanged = bluetoothGatt!!.writeCharacteristic(
                    ledChar!!,
                    colorData,
                    BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                )*/
                Toast.makeText(this, isRedChanged.toString(), Toast.LENGTH_LONG)
            }
        }

        btBlue.setOnClickListener {
            if (bluetoothGatt != null) {
                val colorData = ByteArray(5)
                colorData[0] = Utils.BREATHE
                colorData[1] = LED_BLUE.toByte()
                colorData[2] = 50.toByte()
                Utils.setValue(
                    colorData,
                    3,
                    ledBreatheDelay,
                    BluetoothGattCharacteristic.FORMAT_UINT16
                )
                ledChar.value = colorData
                ledChar.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                bluetoothGatt!!.writeCharacteristic(
                    ledChar!!,
                    colorData,
                    BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                )
            }
        }

        var intent = Intent(this, BleActivity::class.java)
        startActivity(intent)
    }

    private fun scanLeDevice() {
        if (!scanning) { // Stops scanning after a pre-defined scan period.
            handler.postDelayed({
                scanning = false
                bluetoothLeScanner.stopScan(leScanCallback)
            }, SCAN_PERIOD)

            val settings: ScanSettings = ScanSettings.Builder()
                //.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).setReportDelay(1000)
                .build()
            val filters: MutableList<ScanFilter> = ArrayList()
            filters.add(
                ScanFilter.Builder().setServiceUuid(ParcelUuid(Utils.THINGY_BASE_UUID)).build()
            )

            scanning = true
            bluetoothLeScanner.startScan(filters, settings, leScanCallback)
            // bluetoothLeScanner.startScan(leScanCallback)
        } else {
            scanning = false
            bluetoothLeScanner.stopScan(leScanCallback)
        }
    }

    private fun setupRecyclerView(scanResults: List<ScanResult>) {
        var scanAdapter = ScanAdapter(scanResults, this)
        scan_results_recycler_view.layoutManager = LinearLayoutManager(this)
        scan_results_recycler_view.adapter = scanAdapter
        scanAdapter.notifyDataSetChanged()
    }

    private val bluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            bluetoothGatt = gatt

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // successfully connected to the GATT Server
                connectionState = BluetoothLeService.STATE_CONNECTED
                broadcastUpdate(BluetoothLeService.ACTION_GATT_CONNECTED)
                Log.i("BleConnection", "Connected to GATT server.")
                bluetoothGatt!!.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // disconnected from the GATT Server
                connectionState = BluetoothLeService.STATE_DISCONNECTED
                broadcastUpdate(BluetoothLeService.ACTION_GATT_DISCONNECTED)
                Log.e("BleConnection", "Disconnected from GATT server.")
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            bluetoothGatt = gatt

            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED)
                val service: BluetoothGattService? = gatt!!.getService(Utils.THINGY_BASE_UUID)
                nameChar = service!!.getCharacteristic(Utils.DEVICE_NAME_CHARACTERISTIC_UUID)

                val mBatteryService = gatt.getService(Utils.BATTERY_SERVICE)
                if (mBatteryService != null) {
                    batteryChar =
                        mBatteryService.getCharacteristic(Utils.BATTERY_SERVICE_CHARACTERISTIC)
                    Log.v("BleBatteryCharacteristic", "Reading battery characteristic")
                }

                val ledService: BluetoothGattService? = gatt!!.getService(THINGY_UI_SERVICE)
                ledChar = ledService!!.getCharacteristic(Utils.LED_CHARACTERISTIC)
                buttonChar = ledService!!.getCharacteristic(Utils.BUTTON_CHARACTERISTIC)

                requestedCharacteristic.add(buttonChar)
                requestedCharacteristic.add(ledChar)
                requestedCharacteristic.add(batteryChar)
                requestedCharacteristic.add(nameChar)

                requestCharacteristics(gatt)
            } else {
                Log.w("BleChar", "onServicesDiscovered received: $status")
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, value, status)
            bluetoothGatt = gatt

            if (characteristic.equals(nameChar)) {
                val s: String = String(value)
                btStopScan.text = s
            } else if (characteristic.equals(batteryChar)) {
                if (batteryChar != null) {
                    val characteristic: BluetoothGattCharacteristic = batteryChar
                    var mBatteryLevel =
                        characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0)
                    tvStatus.text = mBatteryLevel.toString() + "%"
                }
            } else if (characteristic.equals(ledChar)) {
                ledMode = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0)
                ledColorIndex =
                    characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 1)
                ledColorIntensity =
                    characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 2)
                ledBreatheDelay =
                    characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 3)
            } else if (characteristic.equals(buttonChar)) {
                if (buttonChar != null) {
                    val characteristic: BluetoothGattCharacteristic = buttonChar
                    buttonState =
                        characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0)

                    if (buttonState == 0) {
                        btBlue.text = "Released"
                    } else {
                        btBlue.text = "Pressed"
                    }
                }
            }
            requestedCharacteristic.removeAt(requestedCharacteristic.size - 1)

            if (requestedCharacteristic.size > 0) {
                requestCharacteristics(gatt)
            }
        }

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            bluetoothGatt = gatt
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            super.onCharacteristicChanged(gatt, characteristic)
            if (characteristic != null) {

            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            super.onCharacteristicChanged(gatt, characteristic, value)

            /*if (characteristic == buttonChar) {
                buttonState =
                    characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0)
                val intent = Intent(Utils.BUTTON_STATE_NOTIFICATION)
                intent.putExtra(Utils.EXTRA_DEVICE, mBluetoothDevice)
                intent.putExtra(Utils.EXTRA_DATA_BUTTON, buttonState)
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
                broadcastUpdate()
            }*/
        }
    }

    fun requestCharacteristics(gatt: BluetoothGatt) {
        gatt.readCharacteristic(requestedCharacteristic.get(requestedCharacteristic.size - 1))
    }

    override fun onClickDevice(result: ScanResult) {
        deviceAddress = result.device.address
        requestedCharacteristic = arrayListOf()

        bluetoothGatt = result.device.connectGatt(this, false, bluetoothGattCallback)
        // startActivity(Intent(this, MainActivity2::class.java))
    }

    private val gattUpdateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothLeService.ACTION_GATT_CONNECTED -> {
                    //connected = true
                    Toast.makeText(this@MainActivity, "Connected", Toast.LENGTH_LONG)
                    Log.i("BleConnection", "Connected")
                }

                BluetoothLeService.ACTION_GATT_DISCONNECTED -> {
                    //connected = false
                    Toast.makeText(this@MainActivity, "Disconnected", Toast.LENGTH_LONG)
                    Log.i("BleConnection", "Disconnected")
                }

                BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED -> {
                    Toast.makeText(this@MainActivity, "Service Discovered", Toast.LENGTH_LONG)
                    Log.i("BleConnection", "ServiceDiscovered")
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!bluetoothAdapter.isEnabled) {
            promptEnableBluetooth()
        }
        hasRequiredRuntimePermissions()

        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter())
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(gattUpdateReceiver)
    }

    private fun promptEnableBluetooth() {
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(this@MainActivity, "Bluetooth not connected", Toast.LENGTH_LONG)
                requestRelevantRuntimePermissions()
            } else {
                startActivity(enableBtIntent)
            }
        }
    }

    fun Context.hasPermission(permissionType: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permissionType) ==
                PackageManager.PERMISSION_GRANTED
    }

    fun Context.hasRequiredRuntimePermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            hasPermission(Manifest.permission.BLUETOOTH_SCAN) &&
                    hasPermission(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun Activity.requestRelevantRuntimePermissions() {
        if (hasRequiredRuntimePermissions()) {
            return
        }
        when {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.S -> {
                requestLocationPermission()
            }

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                requestBluetoothPermissions()
            }
        }
    }

    private fun requestLocationPermission() {
        var alert = AlertDialog.Builder(this)
        alert.setTitle("Location Permission Required")
        alert.setMessage("Starting from Android M (6.0), the system requires apps to be granted, location access in order to scan for BLE devices.")
        alert.setCancelable(false)
        alert.setPositiveButton("OK",
            DialogInterface.OnClickListener { dialog, which ->
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    RUNTIME_PERMISSION_REQUEST_CODE
                )
                dialog.cancel()
            })
        var aDialog = alert.create()
        aDialog.show()
    }

    private fun requestBluetoothPermissions() {
        var alert = AlertDialog.Builder(this)
        alert.setTitle("Bluetooth permissions required")
        alert.setMessage("Starting from Android 12, the system requires apps to be granted, Bluetooth access in order to scan for and connect to BLE devices.")
        alert.setCancelable(false)
        alert.setPositiveButton("OK",
            DialogInterface.OnClickListener { dialog, which ->
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ),
                    RUNTIME_PERMISSION_REQUEST_CODE
                )
                dialog.cancel()
            })
        var aDialog = alert.create()
        aDialog.show()
    }

    private fun makeGattUpdateIntentFilter(): IntentFilter? {
        return IntentFilter().apply {
            addAction(BluetoothLeService.ACTION_GATT_CONNECTED)
            addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED)
            addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED)
        }
    }

    private fun broadcastUpdate(action: String) {
        val intent = Intent(action)
        sendBroadcast(intent)
        Log.e("BleBroadcast", "Send Broadcast.")
    }

    private enum class RequestType {
        READ_CHARACTERISTIC, READ_DESCRIPTOR, WRITE_CHARACTERISTIC, WRITE_DESCRIPTOR
    }

    override fun onDestroy() {
        super.onDestroy()
        if (bluetoothGatt != null) {
            bluetoothGatt!!.close()
            bluetoothGatt = null
            requestedCharacteristic = ArrayList()
        }
    }
}