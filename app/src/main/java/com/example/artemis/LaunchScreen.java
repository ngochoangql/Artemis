package com.example.artemis;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.util.Timer;
import java.util.TimerTask;
import java.net.URL;
public class LaunchScreen extends AppCompatActivity {
    private static final int SPLASH_DISPLAY_LENGTH = 2000; // 2 seconds
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch_screen);


        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                // Chuyển đến màn hình chính sau 2 giây
                if ( checkInternetConnection(LaunchScreen.this)){
                    if( checkServerConnection()){
                        Intent mainIntent1 = new Intent(LaunchScreen.this, LoginScreen.class);
                        startActivity(mainIntent1);
                        finish();
                    }else{
                        Intent mainIntent2 = new Intent(LaunchScreen.this, ErrorConnectionScreen.class);
                        startActivity(mainIntent2);
                        finish();
                    }
                }else{
                    Intent mainIntent3 = new Intent(LaunchScreen.this, ErrorConnectionScreen.class);
                    startActivity(mainIntent3);
                    finish();
                }
            }
        };

        // Tạo một Timer và chạy TimerTask sau 2 giây
        Timer timer = new Timer();
        timer.schedule(task, 200); //
    }
    public static boolean checkInternetConnection(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
                return capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
            } else {
                Network[] networks = connectivityManager.getAllNetworks();
                for (Network network : networks) {
                    NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
                    if (capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))) {
                        return true;
                    }
                }
            }
        }
        return false;

    }

    // Kiểm tra kết nối đến máy chủ
    public static boolean checkServerConnection() {
        HttpURLConnection urlc = null;
        try {
            URL url = new URL("http://192.168.1.20:5000/");
            urlc = (HttpURLConnection) url.openConnection();
            urlc.setRequestMethod("GET");
            urlc.setConnectTimeout(500); // Đặt thời gian chờ kết nối là 3 giây (3000 milliseconds)
            urlc.connect();
            int code = urlc.getResponseCode();
            return (code == HttpURLConnection.HTTP_OK);
        } catch (IOException | RuntimeException e) {
            return false;
        } finally {
            if (urlc != null) {
                urlc.disconnect();
            }
        }
    }
}