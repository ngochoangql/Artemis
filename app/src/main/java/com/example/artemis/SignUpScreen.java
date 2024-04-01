package com.example.artemis;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.net.URL;
public class SignUpScreen extends AppCompatActivity {

    EditText username,password,confirmPassword,email;
    TextView tv;
    ConstraintLayout okSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_screen);
        Init();
        okSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String usernameString = username.getText().toString();
                String emailString = email.getText().toString();
                String passwordString = password.getText().toString();
                String confirmPasswordString = confirmPassword.getText().toString();
                if (!passwordString.equals(confirmPasswordString)) {
                    Toast.makeText(SignUpScreen.this, "Mật khẩu và xác nhận mật khẩu không khớp", Toast.LENGTH_SHORT).show();
                    return;
                }

                Executor executor = Executors.newSingleThreadExecutor();
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            sendSignUpRequest(usernameString, emailString, passwordString);
                        } catch (IOException e) {
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tv.setText(e.toString());
                                    Toast.makeText(SignUpScreen.this,"Đã xảy ra lỗi khi đăng ký người dùng.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });

            }
        });


    }
    private void Init(){
        username = findViewById(R.id.usernameSignUp);
        password = findViewById(R.id.passwordSignUp);
        confirmPassword = findViewById(R.id.passwordSignUpConfirm);
        email = findViewById(R.id.emailSignUp);
        okSignUp = findViewById(R.id.okSignUp);
        tv = findViewById(R.id.textView4);
    }
    private void sendSignUpRequest(String username, String email, String password) throws IOException {

        URL url = new URL("http://192.168.1.4:5000/user/signup");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);

        String postData = "{\"username\":\"" + username + "\",\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";

        OutputStream os = conn.getOutputStream();
        os.write(postData.getBytes(StandardCharsets.UTF_8));
        os.flush();
        os.close();

        int responseCode = conn.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {


            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(SignUpScreen.this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                    Intent mainIntent = new Intent(SignUpScreen.this, LoginScreen.class);
                    startActivity(mainIntent);
                    finish();
                }
            });
        } else if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(SignUpScreen.this, "Email đã tồn tại", Toast.LENGTH_SHORT).show();

                }
            });
        } else {
            throw new IOException("Unexpected response code: " + responseCode);
        }
    }
}