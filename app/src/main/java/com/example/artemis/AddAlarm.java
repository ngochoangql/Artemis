package com.example.artemis;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.artemis.Component.DataHolder;
import com.example.artemis.Data.AlarmData;
import com.example.artemis.Data.DeviceData;
import com.example.artemis.Data.JsonFileHelper;
import com.example.artemis.Data.UserDatabase;
import com.example.artemis.Mqtt.MqttHandler;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.UUID;

public class AddAlarm extends AppCompatActivity {
    private static final String TAG = "AddAlarm";
    Calendar calendar = Calendar.getInstance();
    int hour = calendar.get(Calendar.HOUR_OF_DAY); // Lấy giờ
    int minute = calendar.get(Calendar.MINUTE);
    int selectedHour;
    int selectedMinute;
    TextView typeOpen,typeClose,deviceName,roomName,timeDelay;
    MqttHandler mqttHandler;
    boolean state;
    public void Init(){
        typeOpen = findViewById(R.id.typeOpen);
        typeClose = findViewById(R.id.typeClose);
        deviceName = findViewById(R.id.deviceNameAlarm);
        roomName = findViewById(R.id.roomNameAlarm);
        timeDelay = findViewById(R.id.timeDelay);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_alarm);

        //Khởi tạo
        Init();

        //Lấy thiết bị được chọn
        DeviceData deviceData = DataHolder.getDevice();

        //Xử lý
        state = deviceData.state;
        deviceName.setText(deviceData.device_name);
        roomName.setText(deviceData.room_name);
        if(state){
            typeClose.setTextColor(Color.GRAY);
            typeOpen.setTextColor(Color.BLACK);
        }else{
            typeClose.setTextColor(Color.BLACK);
            typeOpen.setTextColor(Color.GRAY);
        }
        typeOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                typeClose.setTextColor(Color.GRAY);
                typeOpen.setTextColor(Color.BLACK);
                state = true;
            }
        });
        typeClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                typeClose.setTextColor(Color.BLACK);
                typeOpen.setTextColor(Color.GRAY);
                state = false;
            }
        });


        try {
            String serverUri = DataHolder.getIpMqttServer(); // Địa chỉ của MQTT broker
            String clientId = "artermis"; // Tạo một client id ngẫu nhiên
            String persistenceDir = getApplicationContext().getFilesDir().getAbsolutePath(); // Đường dẫn tới thư mục lưu trữ persistence
            mqttHandler = new MqttHandler(serverUri, clientId,persistenceDir);
        } catch (MqttException e) {
            Log.e(TAG, "Failed to initialize MqttHelper", e);
        }

        // Thiết lập callback cho MqttHelper
        mqttHandler.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Log.d(TAG, "Connection lost");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                String payload = new String(message.getPayload());
                Log.d(TAG, "Received message on topic " + topic + ": " + payload);
                // Xử lý tin nhắn ở đây
                if (topic.equals("smart-plug.add-alarm-reply")){
                    JSONObject jsonObject = new JSONObject(payload);
                    if (jsonObject.getString("relay_status").equals("off")){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(AddAlarm.this,"Tắt thành công",Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                    if (jsonObject.getString("relay_status").equals("on")){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(AddAlarm.this,"Bật thành công",Toast.LENGTH_SHORT).show();
                            }
                        });
//
                    }

                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.d(TAG, "Message delivered");
            }
        });

        // Thiết lập các tùy chọn kết nối MQTT
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setCleanSession(true);
        mqttConnectOptions.setKeepAliveInterval(60);

        // Kết nối tới MQTT broker
        try {
            mqttHandler.connect(mqttConnectOptions, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "Connected to MQTT broker");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e(TAG, "Failed to connect to MQTT broker", exception);
                }
            });
        } catch (MqttException e) {
            Log.e(TAG, "Failed to connect to MQTT broker", e);
        }



        // tạo 1 hoạt động
        FrameLayout okAddAlarm = findViewById(R.id.okAddAlarm);
        okAddAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlarmData alarmData = new AlarmData( UUID.randomUUID().toString(),deviceData.uuid,"user_id",deviceData.room_name,deviceData.device_name,String.format("%02d", selectedHour)+":"+String.format("%02d", selectedMinute),true,1,"daily","Đi chơi","");
                try {
                    mqttHandler.publish("smart-plug.add-alarm","{\"id\":\""+alarmData.uuid+"\",\"device_id\":\""+alarmData.device_id+"\",\"user_id\":\""+alarmData.user_id+"\",\"event_type\":1,\"time_selection\":\""+alarmData.time_selected+"\",\"repeat_type\":\""+alarmData.repeat_type+"\",\"description\":\""+alarmData.description+"\",\"status\":"+Boolean.toString(alarmData.state)+"}",0,false);
                } catch (MqttException e) {
                    throw new RuntimeException(e);
                }
//                publishMessage("smart-plug.relay-event","aa");

                UserDatabase.getInstance(AddAlarm.this).alarmDataDao().insertAlarm(alarmData);
                Intent intent = new Intent(AddAlarm.this,SmartPlugScreen.class);
                startActivity(intent);
                finish();
            }
        });

        //------------------- Hour------------------
        RecyclerView recyclerViewHour = findViewById(R.id.recyclerViewHours);
        HourAdapter hourAdapter = new HourAdapter(1000);
        recyclerViewHour.setAdapter(hourAdapter);
        recyclerViewHour.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewHour.scrollToPosition(960+hour);
        recyclerViewHour.post(new Runnable() {
            @Override
            public void run() {
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerViewHour.getLayoutManager();
                View viewMiddle = layoutManager.findViewByPosition(960+hour);
                if (viewMiddle != null) {
                    recyclerViewHour.smoothScrollBy(0, viewMiddle.getTop() - (recyclerViewHour.getHeight() / 2) + (viewMiddle.getHeight() / 2));
                }
            }
        });
        recyclerViewHour.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int recyclerViewHeight = recyclerView.getHeight();
                int centerItemPosition = getCenterItemPosition(recyclerView);
                for (int i = 0; i < recyclerView.getChildCount(); i++) {
                    View itemView = recyclerView.getChildAt(i);
                    int itemPosition = recyclerView.getChildLayoutPosition(itemView);
                    TextView textView = itemView.findViewById(R.id.textViewHour);
                    if (itemPosition == centerItemPosition) {
                        selectedHour = centerItemPosition%24;
                        textView.setTextColor(Color.BLACK);
                        timeDelayText();
                        textView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                    } else {
                        textView.setTextColor(Color.GRAY);
                        textView.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                    }
                }
            }
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    int selectedPosition = getCenterItemPosition(recyclerView);
                    View selectedView = recyclerView.getLayoutManager().findViewByPosition(selectedPosition);
                    int selectedViewHeight = selectedView.getHeight();
                    int recyclerViewHeight = recyclerView.getHeight();
                    int scrollDistance = selectedView.getTop() - (recyclerViewHeight / 2) + (selectedViewHeight / 2);
                    recyclerView.smoothScrollBy(0, scrollDistance);
                }
            }
        });
        RecyclerView recyclerViewMinute = findViewById(R.id.recyclerViewMinutes);
        MinuteAdapter minuteAdapter = new MinuteAdapter(1000);
        recyclerViewMinute.setAdapter(minuteAdapter);
        recyclerViewMinute.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMinute.scrollToPosition(960+minute);
        recyclerViewMinute.post(new Runnable() {
            @Override
            public void run() {
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerViewMinute.getLayoutManager();
                View viewMiddle = layoutManager.findViewByPosition(960+minute);
                if (viewMiddle != null) {
                    recyclerViewMinute.smoothScrollBy(0, viewMiddle.getTop() - (recyclerViewMinute.getHeight() / 2) + (viewMiddle.getHeight() / 2));
                }
            }
        });
        recyclerViewMinute.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int recyclerViewHeight = recyclerView.getHeight();
                int centerItemPosition = getCenterItemPosition(recyclerView);
                for (int i = 0; i < recyclerView.getChildCount(); i++) {
                    View itemView = recyclerView.getChildAt(i);
                    int itemPosition = recyclerView.getChildLayoutPosition(itemView);
                    TextView textView = itemView.findViewById(R.id.textViewMinute);
                    if (itemPosition == centerItemPosition) {
                        selectedMinute = centerItemPosition%60;
                        timeDelayText();
                        textView.setTextColor(Color.BLACK);
                        textView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                    } else {
                        textView.setTextColor(Color.GRAY);
                        textView.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                    }
                }
            }
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE || newState == RecyclerView.SCREEN_STATE_ON) {
                    int selectedPosition = getCenterItemPosition(recyclerView);
                    View selectedView = recyclerView.getLayoutManager().findViewByPosition(selectedPosition);
                    int selectedViewHeight = selectedView.getHeight();
                    int recyclerViewHeight = recyclerView.getHeight();
                    int scrollDistance = selectedView.getTop() - (recyclerViewHeight / 2) + (selectedViewHeight / 2);
                    recyclerView.smoothScrollBy(0, scrollDistance);
                }
            }
        });
    }
    @Override
    protected void onDestroy(){
//        mqttHandler.disconnect();
        super.onDestroy();
    }

    private int getCenterItemPosition(RecyclerView recyclerView) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
        int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
        return (firstVisibleItemPosition + lastVisibleItemPosition) / 2;
    }

    @SuppressLint("SetTextI18n")
    private void timeDelayText(){
        int currentHour = calendar.get(Calendar.HOUR)+12;
        int currentMinute = calendar.get(Calendar.MINUTE);

        if (selectedHour==currentHour){
            if(selectedMinute==currentMinute)
                timeDelay.setText("Báo thức sau 23 giờ "+Integer.toString(59)+" phút");
            if(selectedMinute<currentMinute)
                timeDelay.setText("Báo thức sau 23 giờ "+Integer.toString(59-(currentMinute-selectedMinute))+" phút");
            if(selectedMinute>currentMinute)
                timeDelay.setText("Báo thức sau "+Integer.toString(59-(selectedMinute-currentMinute))+" phút");
        }
        if (selectedHour==currentHour+1){
            if(selectedMinute==currentMinute)
                timeDelay.setText("Báo thức sau "+Integer.toString(59)+" phút");
            if(selectedMinute<currentMinute)
                timeDelay.setText("Báo thức sau "+Integer.toString(59-(currentMinute-selectedMinute))+" phút");
            if(selectedMinute==currentMinute+1)
                timeDelay.setText("Báo thức sau 1 giờ");
            if(selectedMinute>currentMinute+1)
                timeDelay.setText("Báo thức sau 1 giờ "+Integer.toString(selectedMinute-currentMinute-1)+" phút");
        }
        if (selectedHour>currentHour+1){
            if(selectedMinute==currentMinute)
                timeDelay.setText("Báo thức sau "+Integer.toString(selectedHour-currentHour-1)+" giờ "+Integer.toString(59)+" phút");
            if(selectedMinute<currentMinute)
                timeDelay.setText("Báo thức sau "+Integer.toString(selectedHour-currentHour-1)+" giờ "+Integer.toString(59-(currentMinute-selectedMinute))+" phút");
            if(selectedMinute==currentMinute+1)
                timeDelay.setText("Báo thức sau "+Integer.toString(selectedHour-currentHour)+" giờ");
            if(selectedMinute>currentMinute+1)
                timeDelay.setText("Báo thức sau "+Integer.toString(selectedHour-currentHour)+" giờ "+Integer.toString(selectedMinute-currentMinute-1)+" phút");
        }
        if (selectedHour<currentHour){
            if(selectedMinute==currentMinute)
                timeDelay.setText("Báo thức sau "+Integer.toString(23-(currentHour-selectedHour))+" giờ "+Integer.toString(59)+" phút");
            if(selectedMinute<currentMinute)
                timeDelay.setText("Báo thức sau "+Integer.toString(23-(currentHour-selectedHour))+" giờ "+Integer.toString(59-(currentMinute-selectedMinute))+" phút");
            if(selectedMinute==currentMinute+1)
                timeDelay.setText("Báo thức sau "+Integer.toString(23-(currentHour-selectedHour)+1)+" giờ");
            if(selectedMinute>currentMinute+1)
                timeDelay.setText("Báo thức sau "+Integer.toString(23-(currentHour-selectedHour))+" giờ "+Integer.toString(selectedMinute-currentMinute-1)+" phút");
        }
    }
}