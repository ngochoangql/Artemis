package com.example.artemis.Component;

import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class ServerConnectionInfo {
    private String serverUrl = "http://192.168.1.2:5000/";
    private int timeout = 300;

    public boolean isConnectedHttps = false;
    public boolean isConnectedMqtt = true;
    public void checkHttpsConnection(String serverUrl) {

            try {
                URL url = new URL(serverUrl);
                HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                urlc.setRequestMethod("GET");
                urlc.setConnectTimeout(timeout);
                urlc.connect();
                int responseCode = urlc.getResponseCode();
                Log.d("http",Boolean.toString(responseCode == HttpURLConnection.HTTP_OK));
                if(responseCode == HttpURLConnection.HTTP_OK){
                    isConnectedHttps = true;
                }

            } catch (IOException | RuntimeException e) {
                e.printStackTrace();
                // Sleep for a while before retrying
                isConnectedHttps = false;
            }
    }
    public void checkMqttConnection(String serverUrl) {
        try {
            // Tạo client MQTT
            MqttClient client = new MqttClient(serverUrl, "Artemis", new MemoryPersistence());

            // Thiết lập callback cho client
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
//                    System.out.println("Kết nối MQTT đã bị mất.");
                    isConnectedMqtt = false;
                }


                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    // Xử lý thông điệp nhận được (nếu cần)
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    // Xác nhận rằng thông điệp đã được gửi thành công (nếu cần)
                }
            });
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }

    }
}