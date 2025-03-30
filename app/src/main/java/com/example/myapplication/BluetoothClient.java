package com.example.myapplication;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.util.Log;
import androidx.core.content.ContextCompat;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

//This is and OBJECT that handles the Bluetooth client
//A BluetoothClient object is created in the BluetoothActivity file and the associated functions can be called
public class BluetoothClient {
    private static final String TAG = "BluetoothClient";
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private InputStream inputStream;
    private boolean isListening = false;
    private DataListener dataListener;
    private BluetoothActivity activity;

    // UUID for Serial Port Profile
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String DEVICE_NAME = "raspberrypi"; // Change this to your Pi's name

    public interface DataListener {
        void onDataReceived(String data);
    }

    //Object constructor
    public BluetoothClient(BluetoothActivity activity, DataListener listener) {
        this.activity = activity;
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.dataListener = listener;
    }

    // Connects to the Raspberry Pi via Bluetooth
    public boolean connectToPi() {
        //Check for error cases
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Log.e(TAG, "Bluetooth not available or disabled");
            return false;
        }

        //Check for permission
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "BLUETOOTH_CONNECT permission not granted!");
            return false;
        }

        //Get list of paired Bluetooth Devices
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        BluetoothDevice device = null;
        for (BluetoothDevice d : pairedDevices) {
            if (d.getName().equals(DEVICE_NAME)) {
                device = d;
                break;
            }
        }

        //Error case if Raspberry Pi isn't found in paired devices
        if (device == null) {
            Log.e(TAG, "Raspberry Pi not found in paired devices");
            return false;
        }

        try {
            // Create an RFCOMM Bluetooth socket and connect to the Raspberry Pi
            bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
            bluetoothSocket.connect();
            //Get the input stream for receiving data
            inputStream = bluetoothSocket.getInputStream();
            Log.d(TAG, "Connected to Raspberry Pi!");
            //Start listening for data
            startListening();
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Could not connect: " + e.getMessage());
            return false;
        }
    }


    private void startListening() {
        isListening = true;
        new Thread(() -> {
            try {
                //Temp buffer to store the received data
                byte[] buffer = new byte[1024];
                int bytes;
                while (isListening) {
                    //Read the data in the buffer and convert to String
                    bytes = inputStream.read(buffer);
                    if (bytes > 0) {
                        String receivedData = new String(buffer, 0, bytes);
                        Log.d(TAG, "Data received: " + receivedData);
                        if (dataListener != null) {
                            //Pass data to listener
                            dataListener.onDataReceived(receivedData);
                        }
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "Error receiving data: " + e.getMessage());
            }
        }).start();
    }

    //Close Bluetooth Connection
    public void closeConnection() {
        try {
            isListening = false;
            if (bluetoothSocket != null) {
                bluetoothSocket.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error closing connection: " + e.getMessage());
        }
    }
}
