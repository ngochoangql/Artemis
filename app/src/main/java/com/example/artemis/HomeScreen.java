package com.example.artemis;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.artemis.Component.QRCodeGenerator;
import com.example.artemis.Data.AppPreferences;
import com.example.artemis.Data.JsonFileHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class HomeScreen extends AppCompatActivity {
    List<JSONObject> objectList = new ArrayList<>();
    private RecyclerView recyclerView;
    private FriendAdapter friendAdapter;
    JsonFileHelper jsonFileHelper = new JsonFileHelper();
    String fragmentShow;
    Fragment houseFragment,myFragment,alarmFragment,analyticsFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        // Khởi tạo 4 fragment
        houseFragment = new House();
        myFragment = new My();
        alarmFragment = new Alarm();
        analyticsFragment = new Analytics();

        // --------------------------Analytics-----------------------------
        ConstraintLayout analyticsButton = findViewById(R.id.analyticsId);
        boolean shouldShowFragment = getIntent().getBooleanExtra("show_house", false);
        if (shouldShowFragment) {
            // Thay đổi Fragment hiển thị
            fragmentShow = "house";
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_home_main, houseFragment)
                    .commit();
        }
        analyticsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chuyển đến Activity quét mã QR
                fragmentShow = "analytics";
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_home_main, analyticsFragment).commit();

            }
        });
        // --------------------------Alarm-----------------------------
        ConstraintLayout alarmButton = findViewById(R.id.alarmId);
        alarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                fragmentShow = "alarm";
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_home_main, alarmFragment).commit();

            }
        });
        // --------------------------House-----------------------------
        ConstraintLayout houseButton = findViewById(R.id.houseId);
        houseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chuyển đến Activity quét mã QR
                fragmentShow = "house";
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_home_main, houseFragment).commit();

            }
        });
        // --------------------------My-----------------------------
        ConstraintLayout myButton = findViewById(R.id.myId);
        myButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chuyển đến Activity quét mã QR
                fragmentShow = "my";
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_home_main, myFragment).commit();

            }
        });
        // --------------------------ScanQr-----------------------------
        ConstraintLayout scanQrButton = findViewById(R.id.scanQrButton);
        scanQrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chuyển đến Activity quét mã QR

                Intent intent = new Intent(HomeScreen.this, ScanQrScreen.class);
                startActivity(intent);
            }
        });

    }

    public void getFriend() {
        try {
            // Tạo URL từ địa chỉ
            URL url = new URL("http://192.168.1.4:5000/friend/get/"+jsonFileHelper.getValueByKey(HomeScreen.this,"data.json","_id"));

            // Mở kết nối HTTP
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            Log.d("http","http://192.168.1.4:5000/friend/get/"+jsonFileHelper.getValueByKey(HomeScreen.this,"data.json","_id"));
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {

                                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                                StringBuilder response = new StringBuilder();
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    response.append(line);
                                }
                                reader.close();

                                // Phân tích dữ liệu JSON
                                JSONArray jsonArray = new JSONArray(response.toString());

                                // Chuyển đổi JSON thành danh sách đối tượng

                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    objectList.add(jsonObject);
                                }
                                Log.d("http",objectList.toString());
                                friendAdapter = new FriendAdapter(objectList);
                                recyclerView.setAdapter(friendAdapter);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                }


                // In ra danh sách đối tượng


                // Đóng kết nối
                connection.disconnect();

            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onBackPressed() {
        // Lấy Fragment hiện tại đang được hiển thị
        Fragment fragment = (Fragment) getSupportFragmentManager().findFragmentById(R.id.fragment_home_main);



        // Kiểm tra xem Fragment đó có tồn tại và có thể xử lý sự kiện "trở lại" không
        if (fragment != null) {
            if (!fragmentShow.equals("my")){
                fragmentShow = "my";
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_home_main, myFragment).commit();
            }else{
                super.onBackPressed();
            }
        } else {
            // Nếu không, thực hiện hành động mặc định của "trở lại", chẳng hạn như đóng Activity hoặc Fragment
            super.onBackPressed();
        }
    }


}