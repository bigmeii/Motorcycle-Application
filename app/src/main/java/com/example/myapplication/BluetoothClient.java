package com.example.myapplication;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.content.ContextCompat;

import java.util.UUID;

public class BluetoothClient {
    private static final String TAG = "BluetoothClient";
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private DataListener dataListener;
    private BluetoothActivity activity;

    private static final String DEVICE_NAME = "raspberrypi"; // Change this to match your Pi's BLE name
    private static final UUID SERVICE_UUID = UUID.fromString("0000180F-0000-1000-8000-00805F9B34FB"); // Example UUID
    private static final UUID CHARACTERISTIC_UUID = UUID.fromString("00002A19-0000-1000-8000-00805F9B34FB"); // Example UUID

    public interface DataListener {
        void onDataReceived(String data);
    }

    public BluetoothClient(BluetoothActivity activity, DataListener listener) {
        this.activity = activity;
        this.dataListener = listener;
        BluetoothManager bluetoothManager = (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
        this.bluetoothAdapter = bluetoothManager.getAdapter();
    }

    public void startScan() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Log.e(TAG, "Bluetooth not available or disabled");
            return;
        }

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_SCAN)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "BLUETOOTH_SCAN permission not granted!");
            return;
        }

        bluetoothAdapter.startLeScan((device, rssi, scanRecord) -> {
            if (device.getName() != null && device.getName().equals(DEVICE_NAME)) {
                bluetoothAdapter.stopLeScan(null);
                connectToDevice(device);
            }
        });
    }

    private void connectToDevice(BluetoothDevice device) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT)
        != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "BLUETOOTH_CONNECT permission not granted");
        }
        bluetoothGatt = device.connectGatt(activity, false, new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    Log.d(TAG, "Connected to BLE device");
                    if (ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT)
                            != PackageManager.PERMISSION_GRANTED) {
                        Log.e(TAG, "BLUETOOTH_CONNECT permission not granted");
                    }
                    gatt.discoverServices();
                } else {
                    Log.e(TAG, "Disconnected from BLE device");
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    BluetoothGattService service = gatt.getService(SERVICE_UUID);
                    if (service != null) {
                        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT)
                                != PackageManager.PERMISSION_GRANTED) {
                            Log.e(TAG, "BLUETOOTH_CONNECT permission not granted");
                        }
                        BluetoothGattCharacteristic characteristic = service.getCharacteristic(CHARACTERISTIC_UUID);
                        gatt.setCharacteristicNotification(characteristic, true);
                    }
                }
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                String receivedData = new String(characteristic.getValue());
                Log.d(TAG, "Data received: " + receivedData);
                if (dataListener != null) {
                    dataListener.onDataReceived(receivedData);
                }
            }
        });
    }

    public void closeConnection() {
        if (bluetoothGatt != null) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "BLUETOOTH_CONNECT permission not granted");
            }
            bluetoothGatt.close();
            bluetoothGatt = null;
        }
    }
}





//package com.example.myapplication;
//
//import android.Manifest;
//import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothDevice;
//import android.bluetooth.BluetoothSocket;
//import android.content.pm.PackageManager;
//import android.util.Log;
//import androidx.core.content.ContextCompat;
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.Set;
//import java.util.UUID;
//
////This is and OBJECT that handles the Bluetooth client
////A BluetoothClient object is created in the BluetoothActivity file and the associated functions can be called
//public class BluetoothClient {
//    private static final String TAG = "BluetoothClient";
//    private BluetoothAdapter bluetoothAdapter;
//    private BluetoothSocket bluetoothSocket;
//    private InputStream inputStream;
//    private boolean isListening = false;
//    private DataListener dataListener;
//    private BluetoothActivity activity;
//
//    // UUID for Serial Port Profile
//    private static final UUID MY_UUID = UUID.fromString("00001800-0000-1000-8000-00805F9B34FB");
//    private static final String DEVICE_NAME = "raspberrypi"; // Change this to your Pi's name
//
//    public interface DataListener {
//        void onDataReceived(String data);
//    }
//
//    //Object constructor
//    public BluetoothClient(BluetoothActivity activity, DataListener listener) {
//        this.activity = activity;
//        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        this.dataListener = listener;
//    }
//
//    // Connects to the Raspberry Pi via Bluetooth
//    public boolean connectToPi() {
//        //Check for error cases
//        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
//            Log.e(TAG, "Bluetooth not available or disabled");
//            return false;
//        }
//
//        //Check for permission
//        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT)
//                != PackageManager.PERMISSION_GRANTED) {
//            Log.e(TAG, "BLUETOOTH_CONNECT permission not granted!");
//            return false;
//        }
//
//        //Get list of paired Bluetooth Devices
//        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
//        BluetoothDevice device = null;
//        for (BluetoothDevice d : pairedDevices) {
//            if (d.getName().equals(DEVICE_NAME)) {
//                device = d;
//                break;
//            }
//        }
//
//        //Error case if Raspberry Pi isn't found in paired devices
//        if (device == null) {
//            Log.e(TAG, "Raspberry Pi not found in paired devices");
//            return false;
//        }
//
//        try {
//            // Create an RFCOMM Bluetooth socket and connect to the Raspberry Pi
//            bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
//            bluetoothSocket.connect();
//            //Get the input stream for receiving data
//            inputStream = bluetoothSocket.getInputStream();
//            Log.d(TAG, "Connected to Raspberry Pi!");
//            //Start listening for data
//            startListening();
//            return true;
//        } catch (IOException e) {
//            Log.e(TAG, "Could not connect: " + e.getMessage());
//            return false;
//        }
//    }
//
//
//    private void startListening() {
//        isListening = true;
//        new Thread(() -> {
//            try {
//                //Temp buffer to store the received data
//                byte[] buffer = new byte[1024];
//                int bytes;
//                while (isListening) {
//                    //Read the data in the buffer and convert to String
//                    bytes = inputStream.read(buffer);
//                    if (bytes > 0) {
//                        String receivedData = new String(buffer, 0, bytes);
//                        Log.d(TAG, "Data received: " + receivedData);
//                        if (dataListener != null) {
//                            //Pass data to listener
//                            dataListener.onDataReceived(receivedData);
//                        }
//                    }
//                }
//            } catch (IOException e) {
//                Log.e(TAG, "Error receiving data: " + e.getMessage());
//            }
//        }).start();
//    }
//
//    //Close Bluetooth Connection
//    public void closeConnection() {
//        try {
//            isListening = false;
//            if (bluetoothSocket != null) {
//                bluetoothSocket.close();
//            }
//        } catch (IOException e) {
//            Log.e(TAG, "Error closing connection: " + e.getMessage());
//        }
//    }
//}
