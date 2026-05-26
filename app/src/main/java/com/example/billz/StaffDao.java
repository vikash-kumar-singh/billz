package com.example.billz;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface StaffDao {
    @Insert
    void insert(Staff staff);

    @Query("SELECT * FROM staff")
    List<Staff> getAllStaff();
}
