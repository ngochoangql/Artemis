package com.example.artemis.Data;



import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;
@Dao
public interface DeviceDataDao {
    @Query("SELECT * FROM devices")
    List<DeviceData> getAll();

    @Query("SELECT * FROM devices WHERE uuid = :uuid")
    DeviceData getDeviceByUUID(String uuid);
    @Insert
    void insertDevice(DeviceData DeviceData);
    @Query("DELETE FROM devices WHERE uuid = :uuid")
    void deleteDeviceByUUID(String uuid);
    @Query("UPDATE devices SET state = :state , value_limit = :value_limit,state_limit = :state_limit WHERE uuid = :uuid")
    void updateDeviceByUUID(boolean state,int value_limit,boolean state_limit, String uuid);
    @Query("UPDATE devices SET state = :state WHERE uuid = :uuid")
    void updateStateDeviceByUUID(boolean state, String uuid);
    @Query("UPDATE devices SET state_limit = :state_limit  WHERE uuid = :uuid")
    void updateStateLimitDeviceByUUID(boolean state_limit, String uuid);
    @Query("UPDATE devices SET value_limit = :value  WHERE uuid = :uuid")
    void updateValueLimitDeviceByUUID(int value, String uuid);
}
