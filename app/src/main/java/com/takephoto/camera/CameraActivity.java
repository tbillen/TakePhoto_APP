package com.takephoto.camera;

import static com.takephoto.login.LoginActivity.apiService;
import static com.takephoto.login.LoginActivity.userID;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.camera.core.Camera;
import androidx.camera.core.CameraProvider;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.core.TorchState;
import androidx.camera.lifecycle.ProcessCameraProvider;

import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.Environment;
import android.widget.Toast;

import com.takephoto.databinding.ActivityCameraBinding;
import com.google.common.util.concurrent.ListenableFuture;
import com.takephoto.login.LoginActivity;
import com.takephoto.vendor.SHA256;
import com.takephoto.vendor.apirequest.GetUser;
import com.takephoto.vendor.apirequest.SetImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executor;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CameraActivity extends AppCompatActivity {

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private PreviewView previewView;
    private ActivityCameraBinding binding;
    private ImageCapture imageCapture;
    private Camera camera;
    private CameraSelector cameraSelector;
    private ProcessCameraProvider cameraProvider;
    private Preview preview;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.binding = ActivityCameraBinding.inflate(getLayoutInflater());
        setContentView(this.binding.getRoot());
        checkPermission();

        this.imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .build();
        this.preview = new Preview.Builder().build();


        this.binding.makePhoto.setVisibility(View.VISIBLE);

        this.binding.changeCamera.setOnClickListener(v -> changeCamera());
        this.binding.light.setOnClickListener(v -> switchLight());
        this.previewView = this.binding.makePhoto;
        this.imageCapture = new ImageCapture.Builder().
                setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY).
                build();
        try{
            this.cameraProviderFuture = ProcessCameraProvider.getInstance(this);
            this.cameraProviderFuture.addListener(() -> {
                try {
                    System.out.println("Add listener");
                    this.cameraProvider = cameraProviderFuture.get();
                    // bindPreview(cameraProvider, CameraSelector.LENS_FACING_BACK);
                    startCameraX(cameraProvider,  CameraSelector.LENS_FACING_BACK);
                    this.preview.setSurfaceProvider(previewView.getSurfaceProvider());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, getExecutor());

            this.cameraSelector = new CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build();


            this.binding.takePhoto.setOnClickListener(v -> takePhoto(cameraProvider));
            this.preview.setSurfaceProvider(previewView.getSurfaceProvider());

        }catch(Exception e){
            System.out.println("camera provider error: " + e.getMessage());
        }


    }

    private void switchLight() {
        try{
            this.camera.getCameraControl().enableTorch(
                    this.camera.getCameraInfo().getTorchState().getValue() != TorchState.ON);
        }catch(Exception e){
            Toast.makeText(this, "Torch is not working", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("RestrictedApi")
    private void changeCamera() {
        int facing = getFacing();
        cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(facing)
                .build();
        this.cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                cameraProvider.unbindAll();
                //bindPreview(cameraProvider, facing);
                startCameraX(cameraProvider, facing);
            } catch (Exception e) {
                System.out.println("CameraX:" + " Error switching camera - " + e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @SuppressLint("RestrictedApi")
    private int getFacing() {
        try {
            return (this.cameraSelector.getLensFacing() == CameraSelector.LENS_FACING_BACK)
                    ? CameraSelector.LENS_FACING_FRONT
                    : CameraSelector.LENS_FACING_BACK;

        }catch(NullPointerException e){
            return -1;
        }
    }

    @NonNull
    private Executor getExecutor() {
        return ContextCompat.getMainExecutor(this);
    }

    private void takePhoto(@NonNull ProcessCameraProvider cameraProvider) {
        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(
                getOutputFile()
        ).build();
        System.out.println("this.imageCapture");
        System.out.println(this.imageCapture);
        Toast.makeText(this, "Everything is created", Toast.LENGTH_SHORT).show();
        cameraProvider.unbindAll();
        this.camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, this.imageCapture);
        this.imageCapture.takePicture(
                outputFileOptions, getExecutor(), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                Uri savedUri = outputFileResults.getSavedUri();
                if (savedUri != null) {
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(savedUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

                        background(userID, Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT));

                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(CameraActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(CameraActivity.this, "uri is null", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                exception.printStackTrace();
            }
        });
    }

    @NonNull
    private File getOutputFile() {
        long timestamp = System.currentTimeMillis();
        this.imageCapture = new ImageCapture.Builder().build();
        File storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return new File(storageDirectory, timestamp + ".jpg");
    }

    private void checkPermission() {
        int MY_READ_PERMISSION_CODE = 101;
        if (ContextCompat.checkSelfPermission(CameraActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(CameraActivity.this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE}, MY_READ_PERMISSION_CODE);
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA}, MY_READ_PERMISSION_CODE);
        }
    }

    private void startCameraX(@NonNull ProcessCameraProvider cameraProvider, int selector) {
        cameraProvider.unbindAll();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(selector)
                .build();

        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(this.previewView.getSurfaceProvider());

        this.imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .build();

        this.camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, this.imageCapture);

    }


    private void background(String userID, String image) {
        Call<SetImage> call = apiService.setImage("insert_pic",userID,image);
        call.enqueue(new Callback<SetImage>() {
            @Override
            public void onResponse(@NonNull Call<SetImage> call, @NonNull Response<SetImage> response) {
                if (response.isSuccessful()) {
                    System.out.println("success: " + response.body());
                }else{
                    System.out.println("no success");
                }
            }

            @Override
            public void onFailure(Call<SetImage> call, Throwable t) {
                System.out.println("Error calling");
            }
        });

    }
}
