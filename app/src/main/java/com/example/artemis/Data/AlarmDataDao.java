    package com.example.artemis.Data;

    import androidx.room.Dao;
    import androidx.room.Delete;
    import androidx.room.Insert;
    import androidx.room.Query;

    import java.util.List;
    @Dao
    public interface AlarmDataDao {
        @Query("SELECT * FROM alarms")
        List<AlarmData> getAll();

        @Query("SELECT * FROM alarms WHERE uuid = :uuid")
        List<AlarmData> getAlarmByUUID(String uuid);
        @Query("SELECT * FROM alarms ORDER BY CASE WHEN state = true THEN 0 ELSE 1 END,room_name")
        List<AlarmData> getAlarmsSortedByRoomNameAndState();
        @Query("SELECT * FROM alarms WHERE device_id = :device_id")
        List<AlarmData> getAlarmByDeviceID(String device_id);
        @Query("DELETE FROM alarms WHERE device_id = :device_id")
        void deleteAlarmByDeviceID(String device_id);
        @Insert
        void insertAlarm(AlarmData alarmData);
        @Query("DELETE FROM alarms WHERE uuid = :uuid")
        void deleteAlarmByUUID(String uuid);
        @Query("UPDATE alarms SET " +
                "time_selected = :timeSelected, state = :state, event_type = :eventType,days_repeat = :days_repeat, repeat_type = :repeatType, " +
                "description = :description WHERE uuid = :uuid")
        void updateAlarmByUUID( String timeSelected,
                               String state, String eventType,String days_repeat, String repeatType, String description, String uuid);
        @Query("UPDATE alarms SET state = :state WHERE uuid = :uuid")
        void updateAlarmState(String uuid,boolean state);
    }
