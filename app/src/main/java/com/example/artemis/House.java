package com.example.artemis;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.TextView;

import com.example.artemis.Component.DataHolder;
import com.example.artemis.Data.DeviceData;
import com.example.artemis.Data.UserDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link House#newInstance} factory method to
 * create an instance of this fragment.
 */
public class House extends Fragment {

    TextView bedRoomTextView,livingRoomTextView;
    boolean isItemsDisplayedLivingRoom = true;
    boolean isItemsDisplayedBedRoom = true;
    public House() {
        // Required empty public constructor
    }
    List<DeviceData> mListDevice = new ArrayList<>() ;
    public static House newInstance(String param1, String param2) {
        House fragment = new House();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_house, container, false);
        GridLayout livingRoom = view.findViewById(R.id.layoutLingRoom);
        GridLayout bedRoom = view.findViewById(R.id.layoutBedRoom);
        livingRoomTextView = view.findViewById(R.id.livingRoomId);
        bedRoomTextView= view.findViewById(R.id.bedRoomId);
        mListDevice = UserDatabase.getInstance(view.getContext()).deviceDataDao().getAll();


        for (DeviceData item : mListDevice) {
            View itemView = LayoutInflater.from(view.getContext()).inflate(R.layout.device_item, livingRoom, false);
            // Set thông tin cho itemView nếu cần
            TextView textView = itemView.findViewById(R.id.deviceName);
            FrameLayout frameLayout = itemView.findViewById(R.id.deviceItem);
            textView.setText(item.device_name);


            livingRoom.addView(itemView);
            Drawable backgroundOff = view.getResources().getDrawable(R.drawable.border10dpoff);
            Drawable backgroundOn = view.getResources().getDrawable(R.drawable.border10dpon);
            Log.d("hoang",Boolean.toString(item.state));
            if(item.state){
                frameLayout.setBackground(backgroundOn);
            }else{
                frameLayout.setBackground(backgroundOff);
            }
            frameLayout.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("StaticFieldLeak")
                @Override
                public void onClick(View v) {
                    Log.d("ScanActivity",item.uuid.toString() + " "+item.room_name.toString()+" "+Boolean.toString(item.state) );
                            DataHolder.setDevice(item);
                            // Đặt dữ liệu vào Intent và mở Activity
                            Intent intent = new Intent(getActivity(), AddAlarm.class);
                            startActivity(intent);
                        }


            });
        }
        for (DeviceData item : mListDevice) {
            View itemView = LayoutInflater.from(view.getContext()).inflate(R.layout.device_item, bedRoom, false);
            // Set thông tin cho itemView nếu cần
            TextView textView = itemView.findViewById(R.id.deviceName);
            FrameLayout frameLayout = itemView.findViewById(R.id.deviceItem);
            textView.setText(item.device_name);
            bedRoom.addView(itemView);
            Drawable backgroundOff = view.getResources().getDrawable(R.drawable.border10dpoff);
            Drawable backgroundOn = view.getResources().getDrawable(R.drawable.border10dpon);
            Log.d("hoang",Boolean.toString(item.state));
            if(item.state){
                frameLayout.setBackground(backgroundOn);
            }else{
                frameLayout.setBackground(backgroundOff);
            }
            frameLayout.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("StaticFieldLeak")
                @Override
                public void onClick(View v) {
                    Log.d("ScanActivity",item.uuid.toString() + " "+item.room_name.toString()+" "+Boolean.toString(item.state) );
                    DataHolder.setDevice(item);
                    // Đặt dữ liệu vào Intent và mở Activity
                    Intent intent = new Intent(getActivity(), SmartPlugScreen.class);
                    startActivity(intent);
                }


            });
        }
        livingRoomTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isItemsDisplayedLivingRoom) {
                    livingRoom.removeAllViews();
                    isItemsDisplayedLivingRoom = false;
                }else{
                    for (DeviceData item : mListDevice) {
                        View itemView = LayoutInflater.from(view.getContext()).inflate(R.layout.device_item, livingRoom, false);
                        // Set thông tin cho itemView nếu cần
                        TextView textView = itemView.findViewById(R.id.deviceName);
                        textView.setText(item.device_name);
                        livingRoom.addView(itemView);
                        FrameLayout frameLayout = itemView.findViewById(R.id.deviceItem);
                        Drawable backgroundOff = view.getResources().getDrawable(R.drawable.border10dpoff);
                        Drawable backgroundOn = view.getResources().getDrawable(R.drawable.border10dpon);
                        Log.d("hoang",Boolean.toString(item.state));
                        if(item.state){
                            frameLayout.setBackground(backgroundOn);
                        }else{
                            frameLayout.setBackground(backgroundOff);
                        }
                        frameLayout.setOnClickListener(new View.OnClickListener() {
                            @SuppressLint("StaticFieldLeak")
                            @Override
                            public void onClick(View v) {
                                Log.d("ScanActivity",item.uuid.toString() + " "+item.room_name.toString()+" "+Boolean.toString(item.state) );
                                DataHolder.setDevice(item);
                                // Đặt dữ liệu vào Intent và mở Activity
                                Intent intent = new Intent(getActivity(), AddAlarm.class);
                                startActivity(intent);
                            }


                        });
                    }
                    isItemsDisplayedLivingRoom = true;
                }
            }
        });
        bedRoomTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isItemsDisplayedBedRoom) {
                    bedRoom.removeAllViews();
                    isItemsDisplayedBedRoom = false;
                }else{
                    for (DeviceData item : mListDevice) {
                        View itemView = LayoutInflater.from(view.getContext()).inflate(R.layout.device_item, bedRoom, false);
                        // Set thông tin cho itemView nếu cần
                        TextView textView = itemView.findViewById(R.id.deviceName);
                        textView.setText(item.device_name);
                        bedRoom.addView(itemView);
                        FrameLayout frameLayout = itemView.findViewById(R.id.deviceItem);
                        Drawable backgroundOff = view.getResources().getDrawable(R.drawable.border10dpoff);
                        Drawable backgroundOn = view.getResources().getDrawable(R.drawable.border10dpon);
                        Log.d("hoang",Boolean.toString(item.state));
                        if(item.state){
                            frameLayout.setBackground(backgroundOn);
                        }else{
                            frameLayout.setBackground(backgroundOff);
                        }
                        frameLayout.setOnClickListener(new View.OnClickListener() {
                            @SuppressLint("StaticFieldLeak")
                            @Override
                            public void onClick(View v) {
                                Log.d("ScanActivity",item.uuid.toString() + " "+item.room_name.toString()+" "+Boolean.toString(item.state) );
                                DataHolder.setDevice(item);
                                // Đặt dữ liệu vào Intent và mở Activity
                                Intent intent = new Intent(getActivity(), SmartPlugScreen.class);
                                startActivity(intent);
                            }


                        });
                    }
                    isItemsDisplayedBedRoom = true;
                }
            }
        });
        return view;
    }
    @Override
    public void onResume() {
        super.onResume();
        isItemsDisplayedBedRoom = true;
        isItemsDisplayedLivingRoom = true;
        mListDevice = UserDatabase.getInstance(getContext()).deviceDataDao().getAll();
    }


}