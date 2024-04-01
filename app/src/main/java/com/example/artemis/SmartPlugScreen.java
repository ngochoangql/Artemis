package com.example.artemis;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import com.example.artemis.Component.DataAsync;
import com.example.artemis.Component.DataHolder;
import com.example.artemis.Data.AlarmData;
import com.example.artemis.Data.UserDatabase;
import com.example.artemis.Mqtt.MqttHandler;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;


import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SmartPlugScreen extends AppCompatActivity {
    private static final String TAG = "SmartPlugScreen";
    TextView deviceName,devicePredict,wattage,current,voltage,apparentP,activeP;
    TextView valueLimit;
    Switch switchDevice,switchLimit;
    TextView addAlarm;
    LinearLayout containerLayout;
    FrameLayout deleteDeviceButton;

    List<AlarmData> mAlarmList;
    List<Entry> entries;
    private Timer timer;
    private LineChart lineChart;
    MqttHandler mqttHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_plug_screen);
        lineChart = (LineChart) findViewById(R.id.lineChart);
        lineChart.getXAxis().setEnabled(false);
        lineChart.getAxisRight().setEnabled(false);

        try {
            String serverUri =  DataHolder.getIpMqttServer(); // Địa chỉ của MQTT broker
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
                if (topic.equals("smart-plug.relay-reply")){
                    JSONObject jsonObject = new JSONObject(payload);
                    if (jsonObject.getString("relay_status").equals("off")){
                        UserDatabase.getInstance(SmartPlugScreen.this).deviceDataDao().updateStateDeviceByUUID(false,DataHolder.getDevice().uuid);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                Toast.makeText(SmartPlugScreen.this,"Tắt thành công",Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                    if (jsonObject.getString("relay_status").equals("on")){

                        UserDatabase.getInstance(SmartPlugScreen.this).deviceDataDao().updateStateDeviceByUUID(true,DataHolder.getDevice().uuid);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                Toast.makeText(SmartPlugScreen.this,"Bật thành công",Toast.LENGTH_SHORT).show();
                            }
                        });
//
                    }

                }
                if (topic.equals("smart-plug.relay-event-reply")){
                    JSONObject jsonObject = new JSONObject(payload);
                    if (jsonObject.getString("relay_status").equals("off")){
                        UserDatabase.getInstance(SmartPlugScreen.this).deviceDataDao().updateStateDeviceByUUID(false,DataHolder.getDevice().uuid);

                        processData(false);

                    }
                    if (jsonObject.getString("relay_status").equals("on")){

                        UserDatabase.getInstance(SmartPlugScreen.this).deviceDataDao().updateStateDeviceByUUID(true,DataHolder.getDevice().uuid);
                        processData(true);
//
                    }

                }
                if (topic.equals("smart-plug.delete-device-reply")){
                    JSONObject jsonObject = new JSONObject(payload);
                    if (jsonObject.getString("message").equals("success")){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(SmartPlugScreen.this,"Xóa thành công",Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                    if (jsonObject.getString("message").equals("failed")){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(SmartPlugScreen.this,"Xóa không thành công",Toast.LENGTH_SHORT).show();
                            }
                        });
//
                    }

                }
                if (topic.equals("smart-plug.limit-reply")){
                    JSONObject jsonObject = new JSONObject(payload);
                    if (jsonObject.getString("message").equals("success")){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    UserDatabase.getInstance(SmartPlugScreen.this).deviceDataDao().updateValueLimitDeviceByUUID(Integer.parseInt(jsonObject.getString("value_limit")),DataHolder.getDevice().uuid);
                                    UserDatabase.getInstance(SmartPlugScreen.this).deviceDataDao().updateStateLimitDeviceByUUID(jsonObject.getBoolean("state_limit"),DataHolder.getDevice().uuid);

                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                                DataHolder.setDevice(UserDatabase.getInstance(SmartPlugScreen.this).deviceDataDao().getDeviceByUUID(DataHolder.getDevice().uuid));
                                Toast.makeText(SmartPlugScreen.this,"Cập nhật thành công",Toast.LENGTH_SHORT).show();

                            }
                        });

                    }
                    if (jsonObject.getString("message").equals("failed")){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(SmartPlugScreen.this,"Cập nhật thất bại",Toast.LENGTH_SHORT).show();

                            }
                        });

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


        timer = new Timer();
        Init();

        // Lập lịch cho Timer Task, thực thi sau mỗi 5 giây, và không có thời gian trễ ban đầu
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                DataAsync dataAsync = new DataAsync();
                dataAsync.async(DataHolder.getDevice().uuid);
                LineData lineData = createChartData(dataAsync.listCurrent,dataAsync.listVoltage,dataAsync.listActiveP,dataAsync.listApparentP);
                lineChart.setData(lineData);
                lineChart.invalidate();

            }
        }, 0, 1000);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                DataAsync dataAsync = new DataAsync();
                                dataAsync.async1(DataHolder.getDevice().uuid);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Thực hiện các thay đổi giao diện ở đây
                        current.setText(Double.toString(dataAsync.current));
                        voltage.setText(Double.toString(dataAsync.voltage));
                        apparentP.setText(Double.toString(dataAsync.active));
                        activeP.setText(Double.toString(dataAsync.apparent));
                        devicePredict.setText(dataAsync.predict);
                    }
                });

            }
        },0,1000);
        switchLimit.setChecked(DataHolder.getDevice().state_limit);
        switchLimit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                try {
                    mqttHandler.publish("smart-plug.limit","{\"id\":\"" + DataHolder.getDevice().uuid+ "\",\"value_limit\":\"" + DataHolder.getDevice().value_limit + "\",\"state_limit\":" + (isChecked ? "true" : "false") + "}",0,false);
                } catch (MqttException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        valueLimit.setText(Integer.toString(DataHolder.getDevice().value_limit)) ;
        // Thêm sự kiện OnClickListener cho EditText
        valueLimit.setPaintFlags(valueLimit.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        valueLimit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Hiển thị dialog khi EditText được nhấn
                showDialog();
            }
        });
        switchDevice.setChecked(DataHolder.getDevice().state);
        switchDevice.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d("hoang",Boolean.toString(isChecked)+" "+Boolean.toString(DataHolder.getDevice().state)+ " "+DataHolder.getDevice().uuid);

                publish("smart-plug.relay","{\"id\":\"" + DataHolder.getDevice().uuid+ "\",\"status\":\"" + (isChecked ? "on" : "off") + "\"}");


            }
        });

        deviceName.setText(DataHolder.getDevice().device_name);

        wattage.setText("0");
//        addAlarm.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent1 = new Intent(SmartPlugScreen.this,AddAlarm.class);
//                startActivity(intent1);
//            }
//        });

        deleteDeviceButton = findViewById(R.id.frameLayout3);
        deleteDeviceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserDatabase.getInstance(SmartPlugScreen.this).deviceDataDao().deleteDeviceByUUID(DataHolder.getDevice().uuid);
                UserDatabase.getInstance(SmartPlugScreen.this).alarmDataDao().deleteAlarmByDeviceID(DataHolder.getDevice().uuid);

                publish("smart-plug.delete-device","{\"id\":\"" + DataHolder.getDevice().uuid+"\"}");
                stopTask();
                Intent intent = new Intent(SmartPlugScreen.this,HomeScreen.class);
                intent.putExtra("show_house",true);
                startActivity(intent);
            }
        });
        addAlarm = findViewById(R.id.addAlarm);
        addAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopTask();
                Intent intent = new Intent(SmartPlugScreen.this,AddAlarm.class);
                startActivity(intent);
            }
        });

        containerLayout = findViewById(R.id.linearLayoutAlarm);

        mAlarmList = UserDatabase.getInstance(SmartPlugScreen.this).alarmDataDao().getAlarmByDeviceID(DataHolder.getDevice().uuid);
        LayoutInflater inflater = LayoutInflater.from(this);
        for (AlarmData data : mAlarmList) {
            View itemView = inflater.inflate(R.layout.alarm_item,containerLayout,false);
            TextView deviceName = itemView.findViewById(R.id.textDeviceName);
            TextView roomName = itemView.findViewById(R.id.textRoomName);
            TextView time =  itemView.findViewById(R.id.textTime);
            Switch switchAlarm = itemView.findViewById(R.id.alarmSwitch);
            FrameLayout itemAlarm = itemView.findViewById(R.id.itemAlarm);
            Drawable drawable = getResources().getDrawable(R.drawable.border1);
            Drawable drawable1 = getResources().getDrawable(R.drawable.border2);

            // Thiết lập dữ liệu cho các thành phần trong item
            deviceName.setText(data.device_name);
            roomName.setText(data.room_name);
            time.setText(data.time_selected);
            switchAlarm.setChecked(data.state);
            if(!data.state){
                itemAlarm.setBackground(drawable);
            }else{

                itemAlarm.setBackground(drawable1);
            }
            switchAlarm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    UserDatabase.getInstance(SmartPlugScreen.this).alarmDataDao().updateAlarmState(data.uuid,isChecked);
                    if(!isChecked){
                        itemAlarm.setBackground(drawable);
                    }else{

                        itemAlarm.setBackground(drawable1);
                    }
                }
            });
            // Thiết lập drawable cho itemAlarm (ví dụ)

            containerLayout.addView(itemView);
        }

    }
    private void processData(boolean data) {
        // Xử lý dữ liệu nhận được từ MQTT và cập nhật trạng thái của Switch
        boolean switchState = data;
        if (switchDevice.isChecked() != data){
            switchDevice.setChecked(switchState);
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        disconnet();
        stopTask();

    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d(TAG,"disconnect MQTT");
        stopTask();
        Intent intent = new Intent(SmartPlugScreen.this,HomeScreen.class);
        intent.putExtra("show_house",true);
        startActivity(intent);
    }
    private void showDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.rounder_limit, null);

        // Tạo dialog
        LinearLayout linearLayout = dialogView.findViewById(R.id.layoutLimit);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nhập giá trị");

        // Tạo EditText trong dialog
        final EditText input = new EditText(this);

        input.setPadding(10,10,10,10);
        linearLayout.addView(input);
        builder.setView(linearLayout);

        // Thiết lập nút OK và hủy
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Xử lý dữ liệu nhập từ dialog
                String text = input.getText().toString();
                try {
                    int value = Integer.parseInt(text); // Chuyển đổi chuỗi thành số nguyên
                    if (value > 0) {
                        // Dữ liệu là số nguyên lớn hơn 0
                        valueLimit.setText(String.valueOf(value));
                        mqttHandler.publish("smart-plug.limit", "{\"id\":\"" + DataHolder.getDevice().uuid + "\",\"value_limit\":\"" + value + "\",\"state_limit\":" + switchLimit.isChecked() + "}", 0, false);
                        dialog.dismiss();
                    } else {
                        // Dữ liệu không hợp lệ
                        Toast.makeText(getApplicationContext(), "Giá trị phải là số nguyên lớn hơn 0", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    // Dữ liệu không phải là số nguyên
                    Toast.makeText(getApplicationContext(), "Giá trị phải là số nguyên", Toast.LENGTH_SHORT).show();
                } catch (MqttException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        builder.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Hiển thị dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void disconnet(){
        try {
            mqttHandler.disconnect();
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }

    }
    private void stopTask(){
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
    private LineData createChartData(List<Entry> current,List<Entry> voltage,List<Entry> active,List<Entry> apparent) {
        List<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(createDataSet(current, "Current", Color.RED));
        dataSets.add(createDataSet(voltage, "Voltage", Color.BLUE));
        dataSets.add(createDataSet(active, "ActivePower", Color.YELLOW));
        dataSets.add(createDataSet(apparent, "ApparentPower", Color.parseColor("#FFA500"))); // Màu cam

        return new LineData(dataSets);
    }

    private LineDataSet createDataSet(List<Entry> entries, String label, int color) {
        LineDataSet dataSet = new LineDataSet(entries, label);
        dataSet.setColor(color);
        dataSet.setCircleColor(color);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(false);
        dataSet.setDrawCircles(false);
        dataSet.setValueTextSize(10f);
        dataSet.setDrawValues(false); // Tắt hiển thị giá trị trên điểm dữ liệu
        return dataSet;
    }
    public void publish(String topic,String message) {
        try {
            mqttHandler.publish(topic,message,0,false);
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }
    private void customizeChart() {
        // Tùy chỉnh đối tượng Legend
        Legend legend = lineChart.getLegend();
        legend.setEnabled(true); // Bật hiển thị Legend
        legend.setForm(Legend.LegendForm.LINE); // Đặt dạng của legend thành đường thẳng
        legend.setTextColor(Color.BLACK); // Đặt màu cho văn bản của legend

        // Tùy chỉnh đối tượng XAxis
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setEnabled(false); // Tắt hiển thị XAxis
    }
    private void Init(){
        deviceName = findViewById(R.id.deviceNameSmartPlug);
        devicePredict = findViewById(R.id.deviceResult);
        wattage = findViewById(R.id.wattage);
        voltage = findViewById(R.id.voltageValue);
        current = findViewById(R.id.currentValue);
        activeP = findViewById(R.id.activePValue);
        apparentP = findViewById(R.id.apparentPValue);
        switchDevice = findViewById(R.id.switchDevice);
        valueLimit = findViewById(R.id.valueLimit);
        switchLimit = findViewById(R.id.switchLimit);

    }
}