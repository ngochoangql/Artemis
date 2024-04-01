package com.example.artemis;
import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.example.artemis.Component.DataHolder;
import com.example.artemis.Data.DeviceData;
import com.example.artemis.Data.JsonFileHelper;
import com.example.artemis.Data.UserDatabase;
import com.example.artemis.Mqtt.MqttHandler;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class ScanQrScreen extends AppCompatActivity implements SurfaceHolder.Callback, Camera.PreviewCallback {

    private static final String TAG = "ScanQrScreen";

    private Camera camera;
    private SurfaceHolder surfaceHolder;

    private final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private boolean cameraReleased = false;

    MqttHandler mqttHandler;
    @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_scan_qr_screen);

            // Kiểm tra quyền truy cập máy ảnh
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
            } else {
                // Quyền truy cập máy ảnh đã được cấp, mở camera
                openCamera();
            }

            SurfaceView surfaceView = findViewById(R.id.surfaceView);
            surfaceHolder = surfaceView.getHolder();
            surfaceHolder.addCallback(this);


        try {
            String serverUri = DataHolder.getIpMqttServer();
            String clientId = MqttClient.generateClientId(); // Tạo một client id ngẫu nhiên
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
                if (topic.equals("smart-plug.add-device-reply")){
                    JSONObject jsonObject = new JSONObject(payload);
                    if (jsonObject.getString("message").equals("success")){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ScanQrScreen.this,"Thêm thành công",Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(ScanQrScreen.this,HomeScreen.class);
                                intent.putExtra("show_house",true);
                                startActivity(intent);
                            }
                        });

                    }
                    if (jsonObject.getString("message").equals("failed")){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ScanQrScreen.this,"Thêm thất bại",Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(ScanQrScreen.this,HomeScreen.class);
                                intent.putExtra("show_house",true);
                                startActivity(intent);
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
        }

        private void openCamera() {
            if (!cameraReleased) {
                if (camera == null) {
                    camera = Camera.open();
                    setCameraDisplayOrientation();
                    if (camera != null) {
                        Camera.Parameters params = camera.getParameters();

                        // Chọn kích thước ảnh
                        List<Camera.Size> supportedSizes = params.getSupportedPictureSizes();
                        Camera.Size selectedSize = getOptimalPictureSize(supportedSizes, 1920, 1080);
                        if (selectedSize != null) {
                            params.setPictureSize(selectedSize.width, selectedSize.height);
                        }

                        // Đặt chất lượng ảnh
                        params.setJpegQuality(100); // 100 là chất lượng tốt nhất

                        // Cấu hình tập trung tự động
                        params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

                        camera.setParameters(params);
                    }

                }
            }
        }

        private void releaseCamera() {
            if (camera != null) {
                camera.release();
                camera = null;
            }
        }

        @Override
        public void surfaceCreated(@NonNull SurfaceHolder holder) {
            try {
                camera.setPreviewDisplay(holder);
                camera.setPreviewCallback(this);
                camera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
            // Không cần xử lý ở đây
        }

        @Override
        public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
            if (camera != null && !cameraReleased) {
                camera.setPreviewCallback(null); // Ngừng xử lý dữ liệu hình ảnh
                camera.stopPreview(); // Ngừng hiển thị hình ảnh trước
                camera.release(); // Giải phóng Camera
                cameraReleased = true; // Đánh dấu rằng Camera đã được giải phóng
            }
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera();
                } else {
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            if (!cameraReleased) {
                // Xử lý dữ liệu hình ảnh ở đây
                // Dữ liệu hình ảnh được lưu trong mảng byte[] data
//            Log.d("ScanActivity", "Preview frame received, data length: " + data.length);
                Camera.Size previewSize = camera.getParameters().getPreviewSize();
                int width = previewSize.width;
                int height = previewSize.height;

                // Tạo BinaryBitmap từ dữ liệu hình ảnh nhận được
                PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(data, width, height, 0, 0, width, height, false);
                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

                // Cấu hình giải mã
                Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
                hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);



                // Giải mã mã QR
                MultiFormatReader reader = new MultiFormatReader();
                try {

                    Result result = reader.decode(bitmap, hints);
                    String qrText = result.getText();
                    Log.d("ScanActivity", "QR Code detected: " + qrText);
                    try {

                        JSONObject jsonObject = new JSONObject(qrText);
                        camera.setPreviewCallback(null); // Ngừng xử lý dữ liệu hình ảnh
                        camera.stopPreview(); // Ngừng hiển thị hình ảnh trước
                        camera.release(); // Giải phóng Camera
                        cameraReleased = true; // Đánh dấu rằng Camera đã được giải phóng
                        Executor executor = Executors.newSingleThreadExecutor();
                        executor.execute(new Runnable() {
                            @Override
                            public void run() {
                                JsonFileHelper jsonFileHelper= new JsonFileHelper();
                                try {
                                    ScanQrAddFriend("55",jsonObject.getString("friend_uuid") );
                                } catch (IOException | JSONException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    ScanQrAddDevice(jsonObject.getString("device_id"),"Living Room",jsonObject.getString("device_name"),jsonObject.getBoolean("device_status") );
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                  }
                    catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    // Xử lý dữ liệu mã QR ở đây

                } catch (NotFoundException e) {
                    // Không tìm thấy mã QR trong hình ảnh
                    Log.d("ScanActivity", "No QR Code found");
                }
            }
        }
        private void setCameraDisplayOrientation() {
            if (!cameraReleased) {
                Camera.CameraInfo info = new Camera.CameraInfo();
                Camera.getCameraInfo(0, info); // 0 là camera mặc định, bạn có thể điều chỉnh nếu cần

                int rotation = getWindowManager().getDefaultDisplay().getRotation();
                int degrees = 0;

                switch (rotation) {
                    case Surface.ROTATION_0:
                        degrees = 0;
                        break;
                    case Surface.ROTATION_90:
                        degrees = 90;
                        break;
                    case Surface.ROTATION_180:
                        degrees = 180;
                        break;
                    case Surface.ROTATION_270:
                        degrees = 270;
                        break;
                }

                int result;
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    result = (info.orientation + degrees) % 360;
                    result = (360 - result) % 360;  // compensate the mirror
                } else {  // back-facing
                    result = (info.orientation - degrees + 360) % 360;
                }
                camera.setDisplayOrientation(result);
            }
        }
    // Hàm để chọn kích thước ảnh tối ưu từ danh sách kích thước hỗ trợ
    private Camera.Size getOptimalPictureSize(List<Camera.Size> sizes, int desiredWidth, int desiredHeight) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) desiredWidth / desiredHeight;
        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = desiredHeight;

        // Tìm kích thước tối ưu
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Trả về kích thước tối ưu
        return optimalSize;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
    private void ScanQrAddFriend(String user_uuid,String friend_uuid) throws IOException {
        Log.d("ScanActivity", "QR Code detected: " +friend_uuid );

        URL url = new URL("http://192.168.1.4:5000/friend/add");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);
        String postData = "{\"user_uuid\":\"" + user_uuid + "\",\"friend_uuid\":\"" + friend_uuid + "\"}";
        OutputStream os = conn.getOutputStream();
        os.write(postData.getBytes(StandardCharsets.UTF_8));
        os.flush();
        os.close();


        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    Toast.makeText(ScanQrScreen.this, "Đã gửi lời mời kết bạn", Toast.LENGTH_SHORT).show();
                    Intent mainIntent = new Intent(ScanQrScreen.this, HomeScreen.class);
                    startActivity(mainIntent);
                    finish();
                }
            });
        } else if (responseCode == HttpURLConnection.HTTP_ACCEPTED) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ScanQrScreen.this, "Các bạn đã là bạn bè", Toast.LENGTH_SHORT).show();

                }
            });
        } else if (responseCode == HttpURLConnection.HTTP_NOT_AUTHORITATIVE) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ScanQrScreen.this, "Người dùng không tồn tại", Toast.LENGTH_SHORT).show();

                }
            });
        }else if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ScanQrScreen.this, "Lời mời đã bị từ chối", Toast.LENGTH_SHORT).show();

                }
            });
        }else if (responseCode == HttpURLConnection.HTTP_NOT_AUTHORITATIVE) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ScanQrScreen.this, "Người dùng không tồn tại", Toast.LENGTH_SHORT).show();

                }
            });
        } else {
            throw new IOException("Unexpected response code: " + responseCode);
        }
    }
    private void ScanQrAddDevice(String uuid,String roomName,String deviceName,boolean state ){
        DeviceData deviceData = new DeviceData(uuid,roomName,deviceName,state);
        UserDatabase.getInstance(this).deviceDataDao().insertDevice(deviceData);
        Log.d(TAG,Boolean.toString(state));

        try {
            mqttHandler.publish("smart-plug.add-device","{\"id\":\"" + uuid + "\",\"name\":\"" + deviceName + "\",\"state\":\"" + state + "\"}",0,false);
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }


    }

}
