package com.example.artemis;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.artemis.Data.AppPreferences;
import com.example.artemis.Data.JsonFileHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginScreen extends AppCompatActivity {

    TextView signUpButton;
    FrameLayout okLogin;
    EditText username,password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_login_screen);
        Init();
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signupIntent = new Intent(LoginScreen.this, SignUpScreen.class);
                startActivity(signupIntent);

            }
        });
        okLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String usernameString = username.getText().toString();
                String passwordString = password.getText().toString();
                Executor executor = Executors.newSingleThreadExecutor();
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            sendLoginRequest(usernameString, passwordString);
                        } catch (IOException e) {
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    Toast.makeText(LoginScreen.this, "Đã xảy ra lỗi khi đăng nhập." ,Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });
//                Intent intent = new Intent(LoginScreen.this,HomeScreen.class);
//                startActivity(intent);
            }
        });
    }

    private void Init(){
        signUpButton = findViewById(R.id.signUpButton);
        okLogin = findViewById(R.id.okLogin);
        username = findViewById(R.id.usernameLogin);
        password = findViewById(R.id.passwordLogin);
    }
    private void sendLoginRequest(String username, String password) throws IOException {

        URL url = new URL("http://192.168.1.20:5000/user/login");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);
        String postData = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
        OutputStream os = conn.getOutputStream();
        os.write(postData.getBytes(StandardCharsets.UTF_8));
        os.flush();
        os.close();


        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {


            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {


                    InputStream inputStream = conn.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    String responseData = stringBuilder.toString();
                        Log.d("data",responseData);
                        JsonFileHelper jsonFileHelper = new JsonFileHelper();
                        jsonFileHelper.saveJsonToFile(LoginScreen.this,"data.json", new JSONObject(responseData));
                    // Đóng các luồng và ngắt kết nối
                    bufferedReader.close();
                    inputStream.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    Toast.makeText(LoginScreen.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                    Intent mainIntent = new Intent(LoginScreen.this, HomeScreen.class);
                    startActivity(mainIntent);
                    finish();
                }
            });
        } else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(LoginScreen.this, "Mật khẩu không chính xác", Toast.LENGTH_SHORT).show();

                }
            });
        } else {
            throw new IOException("Unexpected response code: " + responseCode);
        }
    }
}