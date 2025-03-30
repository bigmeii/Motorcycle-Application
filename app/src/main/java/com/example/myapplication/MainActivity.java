package com.example.myapplication;
//import com.example.myapplication.databinding.ActivityMainBinding;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity {

    private static final String PERMISSION_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String PERMISSION_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final String PERMISSION_BLUETOOTH = Manifest.permission.BLUETOOTH;
    private static final String PERMISSION_BLUETOOTH_CONNECT = Manifest.permission.BLUETOOTH_CONNECT;
    private static final int PERMISSION_REQUEST_CODE = 88;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findViewById(R.id.permButton).setOnClickListener(
            v -> requestRuntimePermission()
        );

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED

                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }


        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                        }
                    }
                });




    }

    private void requestRuntimePermission () {
        if (checkSelfPermission(PERMISSION_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            && checkSelfPermission(PERMISSION_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permissions already granted", Toast.LENGTH_LONG).show();
        }
//        else if (ActivityCompat.shouldShowRequestPermissionRationale(this, PERMISSION_FINE_LOCATION)) {
//
//        }
        else {
            ActivityCompat.requestPermissions(this, new String[] {PERMISSION_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
        }
    }
}