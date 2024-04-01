package com.example.artemis.Component;

import com.example.artemis.Data.DeviceData;

public class DataHolder {
    private static DeviceData device;
    private static String ipMqttServer = null;
    private static String ipHttpServer = null;

    public static DeviceData getDevice() {
        return device;
    }

    public static void setDevice(DeviceData deviceData) {
        device = deviceData;
    }
    public static String getIpMqttServer() {return  ipMqttServer;}
    public static void setIpMqttServer(String ipAddressText) {ipMqttServer = ipAddressText;}
    public static String getIpHttpServer() {return  ipHttpServer;}
    public static void setIpHttpServer(String ipAddressText) {ipHttpServer = ipAddressText;}
}