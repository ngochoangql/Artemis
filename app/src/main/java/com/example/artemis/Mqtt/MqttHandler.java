package com.example.artemis.Mqtt;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

public class MqttHandler{

    private static final String TAG = "MqttHelper";

    private MqttClient mqttClient;
    private MqttClientPersistence persistence;

    public MqttHandler(String serverUri, String clientId,String persistenceDir) throws MqttException {
        persistence = new MqttDefaultFilePersistence(persistenceDir);
        mqttClient = new MqttClient(serverUri, clientId,persistence);
    }

    public void setCallback(MqttCallback callback) {
        mqttClient.setCallback(callback);
    }

    public void connect(MqttConnectOptions mqttConnectOptions, IMqttActionListener callback) throws MqttException {
        IMqttToken token = mqttClient.connectWithResult(mqttConnectOptions);
        token.setActionCallback(callback);
        subscribe("smart-plug.relay-reply", 0);
        subscribe("smart-plug.relay-event-reply", 0);
        subscribe("smart-plug.add-device-reply", 0);
        subscribe("smart-plug.async-reply", 0);
        subscribe("smart-plug.delete-device-reply", 0);
        subscribe("smart-plug.limit-reply", 0);

    }

    public void disconnect() throws MqttException {
        mqttClient.disconnect();
    }

    public void publish(String topic, String message, int qos, boolean retained) throws MqttException {
        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setPayload(message.getBytes());
        mqttMessage.setQos(qos);
        mqttMessage.setRetained(retained);
        mqttClient.publish(topic, mqttMessage);
    }

    public void subscribe(String topic, int qos) throws MqttException {
        mqttClient.subscribe(topic, qos);
    }

    public void unsubscribe(String topic) throws MqttException {
        mqttClient.unsubscribe(topic);
    }
}
