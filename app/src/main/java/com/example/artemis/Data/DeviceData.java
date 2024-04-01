package com.example.artemis.Data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "devices")
public class DeviceData {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "uuid")
    public String uuid;
    @ColumnInfo(name = "room_name")
    public String room_name;

    @ColumnInfo(name = "device_name")
    public String device_name;
    @ColumnInfo(name = "state")
    public boolean state;
    @ColumnInfo(name = "value_limit")
    public int value_limit;
    @ColumnInfo(name = "state_limit")
    public boolean state_limit;


    public DeviceData(String uuid,String room_name,String device_name,boolean state){
        this.uuid = uuid;
        this.room_name = room_name;
        this.device_name = device_name;
        this.state = state;
        this.value_limit = 0;
        this.state_limit = false;
    }
}
