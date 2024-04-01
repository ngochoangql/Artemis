package com.example.artemis.Data;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class JsonFileHelper {

    // Phương thức để lưu trữ dữ liệu JSON vào tệp tin
    public void saveJsonToFile(Context context, String fileName, JSONObject jsonObject) {
        try {
            // Chuyển đối tượng JSON thành chuỗi JSON
            String jsonString = jsonObject.toString();
            // Mở một luồng đầu ra để ghi vào tệp tin
            FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            // Ghi chuỗi JSON vào tệp tin
            fos.write(jsonString.getBytes());
            // Đóng luồng
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // Phương thức để đọc dữ liệu từ tệp tin JSON
    public JSONObject readJsonFromFile(Context context, String fileName) {
        try {
            // Mở một luồng đầu vào để đọc từ tệp tin
            FileInputStream fis = context.openFileInput(fileName);
            // Tạo một mảng byte để chứa dữ liệu đọc từ tệp tin
            byte[] buffer = new byte[fis.available()];
            // Đọc dữ liệu từ tệp tin và lưu vào mảng byte
            fis.read(buffer);
            // Đóng luồng
            fis.close();
            // Chuyển đổi mảng byte thành chuỗi
            String jsonString = new String(buffer, "UTF-8");
            Log.d("http",jsonString);
            // Chuyển đổi chuỗi JSON thành đối tượng JSON
            return new JSONObject(jsonString);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
    public String getValueByKey(Context context,String fileName,String key){
        JSONObject jsonObject = readJsonFromFile(context,fileName);
        String value;
        try {
            value = jsonObject.getString(key);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return value;
    }



}
