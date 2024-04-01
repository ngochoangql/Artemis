package com.example.artemis.Component;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.artemis.Data.UserDatabase;
import com.example.artemis.Mqtt.MqttHandler;
import com.example.artemis.SmartPlugScreen;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONObject;

public class Async {
    private static final String TAG = "Async Class";
    MqttHandler mqttHandler;

    public Async(Context context){
        try {
            String serverUri = DataHolder.getIpMqttServer();  // Địa chỉ của MQTT broker
            String clientId = "Artermis";
            String persistenceDir = context.getApplicationContext().getFilesDir().getAbsolutePath(); // Đường dẫn tới thư mục lưu trữ persistence
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
                if (topic.equals("smart-plug.async-reply")){
                    JSONObject jsonObject = new JSONObject(payload);
                    JSONArray devices = jsonObject.getJSONArray("devices");
                    for (int i = 0; i < devices.length(); i++) {
                        JSONObject device = devices.getJSONObject(i);
                        String relayStatus = device.getString("relay_status");
                        int valueLimit = Integer.parseInt(device.getString("value_limit"));
                        boolean stateLimit = Boolean.getBoolean(device.getString("state_limit"));
                        UserDatabase.getInstance(context).deviceDataDao().updateDeviceByUUID(relayStatus.equals("on") ? true : false,valueLimit,stateLimit,device.getString("id"));
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

    }
    public void asyncDevices(){
        try {
            mqttHandler.publish("smart-plug.async","async",0,false);
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }
}
