package com.example.artemis;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class CalendarScreen extends AppCompatActivity {

    TextView bedRoomTextView,livingRoomTextView;
    boolean isItemsDisplayedLivingRoom = true;
    boolean isItemsDisplayedBedRoom = true;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_screen);
        GridLayout livingRoom = findViewById(R.id.LivingRoom);
        GridLayout bedRoom = findViewById(R.id.BedRoom);
        livingRoomTextView = (TextView) findViewById(R.id.textViewLivingRoom);
        bedRoomTextView= (TextView) findViewById(R.id.textViewBedRoom);
        List<String> itemList = new ArrayList<>();
        itemList.add("Item 1");
        itemList.add("Item 2");
        itemList.add("Item 3");
        for (String item : itemList) {
            View itemView = LayoutInflater.from(this).inflate(R.layout.device_item, livingRoom, false);
            // Set thông tin cho itemView nếu cần
            TextView textView = itemView.findViewById(R.id.deviceName);
            textView.setText(item);
            livingRoom.addView(itemView);
        }
        for (String item : itemList) {
            View itemView = LayoutInflater.from(this).inflate(R.layout.device_item, bedRoom, false);
            // Set thông tin cho itemView nếu cần
            TextView textView = itemView.findViewById(R.id.deviceName);
            textView.setText(item);
            bedRoom.addView(itemView);
        }
        livingRoomTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isItemsDisplayedLivingRoom) {
                    livingRoom.removeAllViews();
                    isItemsDisplayedLivingRoom = false;
                }else{
                    for (String item : itemList) {
                        View itemView = LayoutInflater.from(CalendarScreen.this).inflate(R.layout.device_item, livingRoom, false);
                        // Set thông tin cho itemView nếu cần
                        TextView textView = itemView.findViewById(R.id.deviceName);
                        textView.setText(item);
                        livingRoom.addView(itemView);
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
                    for (String item : itemList) {
                        View itemView = LayoutInflater.from(CalendarScreen.this).inflate(R.layout.device_item, bedRoom, false);
                        // Set thông tin cho itemView nếu cần
                        TextView textView = itemView.findViewById(R.id.deviceName);
                        textView.setText(item);
                        bedRoom.addView(itemView);
                    }
                    isItemsDisplayedBedRoom = true;
                }
            }
        });
    }

}