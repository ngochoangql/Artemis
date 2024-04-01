package com.example.artemis;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.artemis.Component.DataHolder;
import com.example.artemis.Data.AlarmData;
import com.example.artemis.Data.UserDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Alarm#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Alarm extends Fragment {
    List<AlarmData> alarmDataList;
    AlarmAdapter alarmAdapter;
    RecyclerView  recyclerView;
    public Alarm() {
        // Required empty public constructor
    }

    public static Alarm newInstance(String param1, String param2) {
        Alarm fragment = new Alarm();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_alarm, container, false);

        alarmDataList = UserDatabase.getInstance(view.getContext()).alarmDataDao().getAlarmsSortedByRoomNameAndState();
        recyclerView = view.findViewById(R.id.alarmRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        alarmAdapter = new AlarmAdapter(alarmDataList);
        recyclerView.setAdapter(alarmAdapter);

        return view;
    }
}