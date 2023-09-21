package com.takephoto.main;

import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.os.Bundle;

import com.takephoto.camera.CameraActivity;
import com.takephoto.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(this.binding.getRoot());

        this.binding.camera.setOnClickListener((v) -> openCamera());

    }

    private void openCamera(){
        Intent intent = new Intent(this, CameraActivity.class);
        finish();
        startActivity(intent);
    }


}