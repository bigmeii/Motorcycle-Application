package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.telephony.SmsManager;

public class BluetoothActivity extends AppCompatActivity implements BluetoothClient.DataListener {
    private BluetoothClient bluetoothClient;
    private TextView dataTextView;
    private Button connectButton;
    EditText emergencyNumber;
    Button setNumBtn;
    Button callButton;
    Button cancelButton;
    Button testButton;
    private Handler callHandler = new Handler(); // Handler to delay call
    private Runnable callRunnable; // Runnable for delayed call
    private static final String PERMISSION_CALL_PHONE = android.Manifest.permission.CALL_PHONE;
    private static final String PERMISSION_SEND_SMS = android.Manifest.permission.SEND_SMS;
    static int PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        //Set variables to reference UI elements
        dataTextView = findViewById(R.id.dataTextView);
        connectButton = findViewById(R.id.connectButton);
        emergencyNumber = findViewById(R.id.emergencyNumberInput);
        cancelButton = findViewById(R.id.cancelButton);

        // 🔹 Pass 'this' to BluetoothClient to check permissions properly
        bluetoothClient = new BluetoothClient(this, this);

        // 🔹 Request Bluetooth permission
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
        }
//        }
        //Request calling and SMS permissions
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {PERMISSION_CALL_PHONE}, PERMISSION_CODE);
        }
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {PERMISSION_SEND_SMS}, 51);
        }

        //Connect to Bluetooth his is different from connecting the phone to the Pi
        //I'm pretty sure this connects the app to the Bluetooth client
        connectButton.setOnClickListener(v -> {
            if (bluetoothClient.connectToPi()) {
                dataTextView.setText("Connected. Waiting for data...");
            } else {
                dataTextView.setText("Failed to connect");
            }
        });

        //Button that appears during delay before call to cancel it
        cancelButton.setOnClickListener(v -> cancelCall());

        //Button to simulate data
        testButton = findViewById(R.id.testButton);
        testButton.setOnClickListener(v -> {
            onDataReceived("1"); // Simulate Pi sending "1"
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bluetoothClient.closeConnection();
    }

    //This runs whenever data is received from Bluetooth
    @Override
    public void onDataReceived(String data) {
        //Change the text in the UI
        runOnUiThread(() -> dataTextView.setText("Received: " + data));

        //Check if the received string is "1": Crash detected
        if (data.equals("1")) {
            //Get the phone number entered in the text box
            String phoneNumber = emergencyNumber.getText().toString();

            //Error case
            if (phoneNumber.isEmpty()) {
                runOnUiThread(() -> dataTextView.setText("Error: No phone number entered."));
                return;
            }

            //Change UI elements
            runOnUiThread(() -> {
                dataTextView.setText("Emergency call in 10 seconds. Tap cancel to stop.");
                cancelButton.setVisibility(View.VISIBLE); // Show cancel button
            });

            // Schedule the call after 10 seconds
            callRunnable = () -> {
                //Check if user gave app permission to call
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                    //Make phone call
                    Intent i = new Intent(Intent.ACTION_CALL);
                    i.setData(Uri.parse("tel:" + phoneNumber));
                    startActivity(i);
                    dataTextView.setText("Emergency call ongoing."); //Change UI element

                    //Send SMS message
                    String emergencyMessage = "Emergency Message";
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(phoneNumber, null, emergencyMessage, null, null);

                } else {
                    //Request permissions if not granted
                    ActivityCompat.requestPermissions(this, new String[]{PERMISSION_CALL_PHONE}, PERMISSION_CODE);
                }
                runOnUiThread(() -> cancelButton.setVisibility(View.GONE)); // Hide cancel button after calling
            };

            //10 second delay
            callHandler.postDelayed(callRunnable, 10000);
        }
    }

    //Cancel call button
    private void cancelCall() {
        callHandler.removeCallbacks(callRunnable); // Stop the delayed call
        runOnUiThread(() -> {
            dataTextView.setText("Call canceled.");
            cancelButton.setVisibility(View.GONE); // Hide cancel button
        });
    }

    //Request permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) { // Bluetooth Permission
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dataTextView.setText("Bluetooth permission granted. Try connecting.");
            } else {
                dataTextView.setText("Bluetooth permission denied. App may not work.");
            }
        } else if (requestCode == PERMISSION_CODE) { // Call Phone Permission
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dataTextView.setText("Call permission granted. Now you can make emergency calls.");
            } else {
                dataTextView.setText("Call permission denied. Cannot make emergency calls.");
            }
        }
    }

}
