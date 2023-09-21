package com.takephoto.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.takephoto.databinding.ActivityLoginBinding;
import com.takephoto.main.MainActivity;
import com.takephoto.vendor.SHA256;
import com.takephoto.vendor.apirequest.ApiService;
import com.takephoto.vendor.apirequest.GetUser;
import com.takephoto.vendor.apirequest.LoginInfo;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    private SharedPreferences sharedPreferences;

    public static final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://tobias-billen.lima-city.de")
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    public static final ApiService apiService = retrofit.create(ApiService.class);

    public static String username = "";
    public static String userID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(this.binding.getRoot());

        this.binding.buttonLogin.setOnClickListener((v) ->login());

        this.sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        this.binding.editTextUsername.setText(this.sharedPreferences.getString("username", ""));
        this.binding.editTextPassword.setText(this.sharedPreferences.getString("password", ""));
    }

    private void login() {
        this.binding.buttonLogin.setVisibility(View.INVISIBLE);
        this.binding.editTextUsername.setVisibility(View.INVISIBLE);
        this.binding.editTextPassword.setVisibility(View.INVISIBLE);
        Thread thread = new Thread(() -> new Handler(Looper.getMainLooper()).post(this::background));
        thread.start();
    }

    private void background() {
        Call<GetUser> call = apiService.getUser("get_user",
                this.binding.editTextUsername.getText().toString(),
                new SHA256().toSH256(this.binding.editTextPassword.getText().toString()));
        call.enqueue(new Callback<GetUser>() {
            @Override
            public void onResponse(@NonNull Call<GetUser> call, @NonNull Response<GetUser> response) {
                if (response.isSuccessful()) {
                    GetUser getUser = response.body();
                    if (getUser != null) {
                        if (getUser.getUsers().size() == 1) {
                            username = binding.editTextUsername.getText().toString();
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("username", binding.editTextUsername.getText().toString());
                            editor.putString("password", binding.editTextPassword.getText().toString());
                            editor.apply();
                            userID = getUser.getUsers().get(0).getId();
                            openMainActivity();
                        } else {
                            binding.buttonLogin.setVisibility(View.VISIBLE);
                            binding.editTextUsername.setVisibility(View.VISIBLE);
                            binding.editTextPassword.setVisibility(View.VISIBLE);
                            Toast.makeText(LoginActivity.this, "Username or password is wrong", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<GetUser> call, @NonNull Throwable t) {
                Toast.makeText(LoginActivity.this, "Error Background", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void openMainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        finish();
        startActivity(intent);
    }

}



