package com.example.artemis.Data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;
@Dao
public interface UserDao {
    @Query("SELECT * FROM users")
    List<User> getAll();
//    @Query("SELECT * FROM users WHERE uuid = uuid")
//    User getUserByUUID(String uuid);
    @Insert
    void insertUser(User user);
}
