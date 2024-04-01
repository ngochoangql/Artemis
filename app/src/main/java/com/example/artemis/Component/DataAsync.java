package com.example.artemis.Component;

import com.example.artemis.HomeScreen;
import com.github.mikephil.charting.data.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class DataAsync {

    public List<Entry> listCurrent ;
    public List<Entry> listVoltage ;
    public List<Entry> listActiveP ;
    public List<Entry> listApparentP ;
    public double current,voltage,active,apparent;
    public String predict;

    public void async(String id){
        URL url = null;

        try {
            url = new URL(DataHolder.getIpHttpServer()+"/data/chart/"+id);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK){
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                JSONObject jsonObject = new JSONObject(response.toString());

                JSONArray dataArrayCurrent = jsonObject.getJSONArray("current");
                List<Entry> entryListCurrent = new ArrayList<>();
                for (int i = 0; i < dataArrayCurrent.length(); i++) {
                    try {
                        float current = (float) dataArrayCurrent.getDouble(i);
                        entryListCurrent.add(new Entry((float) i,current));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                listCurrent = entryListCurrent;
                JSONArray dataArrayVoltage = jsonObject.getJSONArray("voltage");
                List<Entry> entryListVoltage = new ArrayList<>();
                for (int i = 0; i < dataArrayVoltage.length(); i++) {
                    try {
                        float voltage = (float) dataArrayVoltage.getDouble(i);
                        entryListVoltage.add(new Entry((float) i,voltage));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                listVoltage = entryListVoltage;
                JSONArray dataArrayActive = jsonObject.getJSONArray("active");
                List<Entry> entryListActive = new ArrayList<>();
                for (int i = 0; i < dataArrayActive.length(); i++) {
                    try {
                        float active = (float) dataArrayActive.getDouble(i);
                        entryListActive.add(new Entry((float) i,active));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                listActiveP = entryListActive;
                JSONArray dataArrayApparent = jsonObject.getJSONArray("apparent");
                List<Entry> entryListApparent = new ArrayList<>();
                for (int i = 0; i < dataArrayApparent.length(); i++) {
                    try {
                        float apparent = (float) dataArrayApparent.getDouble(i);
                        entryListApparent.add(new Entry((float) i,apparent));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                listApparentP = entryListApparent;
            }


        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (ProtocolException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        // Mở kết nối HTTP

    }
    public void async1(String id){
        URL url = null;

        try {
            url = new URL(DataHolder.getIpHttpServer()+"/data/parameter/"+id);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK){
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                StringBuilder stringBuilder = new StringBuilder();
                JSONObject jsonObject = new JSONObject(response.toString());
                current = jsonObject.getDouble("current");
                voltage = jsonObject.getDouble("voltage");
                active = jsonObject.getDouble("active");
                apparent = jsonObject.getDouble("apparent");
                predict = jsonObject.getString("predict");
            }


        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (ProtocolException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        // Mở kết nối HTTP

    }
}
