package com.example.billz;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface StaffDao {
    @Insert
    void insert(Staff staff);

    @androidx.room.Update
    void update(Staff staff);

    @Query("SELECT * FROM staff")
    List<Staff> getAllStaff();

    @Query("SELECT * FROM staff WHERE id = :id")
    Staff getById(int id);
}
