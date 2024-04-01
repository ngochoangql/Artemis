package com.example.artemis.Data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "alarms")
public class AlarmData {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "uuid")
    public String uuid;
    @ColumnInfo(name = "device_id")
    public String device_id;
    @ColumnInfo(name = "user_id")
    public String user_id;
    @ColumnInfo(name = "room_name")
    public String room_name;

    @ColumnInfo(name = "device_name")
    public String device_name;

    @ColumnInfo(name = "time_selected")
    public String time_selected;

    @ColumnInfo(name = "state")
    public boolean state;
    @ColumnInfo(name = "event_type")
    public int event_type;
    @ColumnInfo(name = "repeat_type")
    public String repeat_type;
    @ColumnInfo(name = "description")
    public String description;
    @ColumnInfo(name = "days_repeat")
    public String days_repeat;

    public AlarmData(String uuid,String device_id,String user_id,String room_name,String device_name,String time_selected,boolean state,int event_type,String repeat_type, String description, String days_repeat){
        this.uuid = uuid;
        this.device_id = device_id;
        this.user_id = user_id;
        this.room_name = room_name;
        this.device_name = device_name;
        this.time_selected = time_selected;
        this.state = state;
        this.event_type = event_type;
        this.repeat_type = repeat_type;
        this.description = description;
        this.days_repeat = days_repeat;
    }
}
