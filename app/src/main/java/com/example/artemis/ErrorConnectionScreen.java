package com.example.artemis;

import android.content.Context;
import android.content.Intent;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.example.artemis.Component.Async;
import com.example.artemis.Component.DataHolder;
import com.example.artemis.Component.ServerConnectionChecker;
import com.example.artemis.Component.ServerConnectionInfo;

public class ErrorConnectionScreen extends AppCompatActivity {
    FrameLayout imageDisconnection;
    TextView textStatusConnection;
    EditText portMqtt,httpServer;
    ConstraintLayout mainLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error_connection_screen);


        Init();
//        check_connection();

        mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                check_connection();
            }
        });
        ConstraintLayout offlineButton = findViewById(R.id.offline);
        portMqtt.setText("19447");
        httpServer.setText("ddfa-171-252-189-149");
        offlineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DataHolder.setIpMqttServer("tcp://0.tcp.ap.ngrok.io:"+portMqtt.getText().toString());
                DataHolder.setIpHttpServer("https://"+httpServer.getText().toString()+".ngrok-free.app");
                if (DataHolder.getIpMqttServer() != null && DataHolder.getIpHttpServer()!= null){
                    if(checkInternetConnection(ErrorConnectionScreen.this)){
                        ServerConnectionInfo serverConnectionInfo = new ServerConnectionInfo();
                        ExecutorService executor = Executors.newSingleThreadExecutor();
                        executor.execute(new Runnable() {
                            @Override
                            public void run() {
                                serverConnectionInfo.checkHttpsConnection(DataHolder.getIpHttpServer());
                                serverConnectionInfo.checkMqttConnection(DataHolder.getIpMqttServer());
                                if (serverConnectionInfo.isConnectedHttps){
                                    if (serverConnectionInfo.isConnectedMqtt){
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Async async = new Async(ErrorConnectionScreen.this);
                                                async.asyncDevices();
                                                Toast.makeText(ErrorConnectionScreen.this,"Kết nối thành công",Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(ErrorConnectionScreen.this,HomeScreen.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        });

                                    }else {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(ErrorConnectionScreen.this,"Server Mqtt disconnect",Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }else {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(ErrorConnectionScreen.this,"Server Https disconnect",Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        });
                        // Đóng ExecutorService sau khi không cần thiết nữa
                        executor.shutdown();
                        try {
                            // Tạm dừng chương trình trong 300ms
                            Thread.sleep(400);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
        });
    }

    private void Init(){
        mainLayout = findViewById(R.id.main);
        imageDisconnection = findViewById(R.id.imageDisconnection);
        textStatusConnection = findViewById(R.id.textStatusConnection);
        portMqtt = findViewById(R.id.ipMqttServer);
        httpServer = findViewById(R.id.ipHttpServer);
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
}