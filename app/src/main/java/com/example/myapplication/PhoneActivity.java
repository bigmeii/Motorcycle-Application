package com.example.myapplication;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class PhoneActivity extends AppCompatActivity {
    EditText emergencyNumber;
    Button setNumBtn;
    Button callButton;
    private static final String PERMISSION_CALL_PHONE = android.Manifest.permission.CALL_PHONE;
    static int PERMISSION_CODE = 100;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_phone);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        emergencyNumber = findViewById(R.id.emergencyNumberInput);
        setNumBtn = findViewById(R.id.emergencyNumberButton);
        callButton = findViewById(R.id.callButton);

        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {PERMISSION_CALL_PHONE}, PERMISSION_CODE);
        }

        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phoneNumber = emergencyNumber.getText().toString();
                Intent i = new Intent(Intent.ACTION_CALL);
                i.setData(Uri.parse("tel:"+phoneNumber));
                startActivity(i);
            }
        });
    }
}