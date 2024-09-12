package com.fmi.bookzz.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.fmi.bookzz.R;

public class StartActivity extends AppCompatActivity {

    Button loginB;
    Button registerB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        loginB = findViewById(R.id.startLoginB);
        registerB = findViewById(R.id.startRegisterB);
    }

    public void onStartLoginCkick(View view){
        startActivity(new Intent(StartActivity.this, LoginActivity.class));
    }
    public void onStartRegisterLoginClick(View view){
        startActivity(new Intent(StartActivity.this, RegisterActivity.class));
    }
}