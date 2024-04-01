package com.example.artemis;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.artemis.Data.AlarmData;
import com.example.artemis.Data.UserDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.ViewHolder> {

    private List<AlarmData> alarmList;

    public AlarmAdapter(List<AlarmData> alarmList) {
        this.alarmList = alarmList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.alarm_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            AlarmData alarmData = alarmList.get(position);
            holder.deviceName.setText(alarmData.device_name);
            holder.roomName.setText(alarmData.room_name);
            holder.time.setText(alarmData.time_selected);
            holder.switchAlarm.setChecked(alarmData.state);
        if(!alarmData.state){
            holder.itemAlarm.setBackground(holder.drawable);
        }else{
            holder.itemAlarm.setBackground(holder.drawable1);
        }
            holder.switchAlarm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    UserDatabase.getInstance(buttonView.getContext()).alarmDataDao().updateAlarmState(alarmData.uuid,isChecked);

                    if(!isChecked){
                        holder.itemAlarm.setBackground(holder.drawable);


                    }else{

                        holder.itemAlarm.setBackground(holder.drawable1);

                    }
                }
            });
    }

    @Override
    public int getItemCount() {
        return alarmList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView deviceName,roomName,time;
        FrameLayout itemAlarm;
        Switch switchAlarm;
        Drawable drawable,drawable1;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceName = itemView.findViewById(R.id.textDeviceName);
            roomName = itemView.findViewById(R.id.textRoomName);
            time =  itemView.findViewById(R.id.textTime);
            switchAlarm = itemView.findViewById(R.id.alarmSwitch);
            itemAlarm = itemView.findViewById(R.id.itemAlarm);
            drawable = itemView.getResources().getDrawable(R.drawable.border1);
            drawable1 = itemView.getResources().getDrawable(R.drawable.border2);
        }
    }
}
