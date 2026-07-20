package com.example.billz;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface StaffDao {
    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    long insert(Staff staff);

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    void insertAll(List<Staff> staffList);

    @androidx.room.Update
    void update(Staff staff);

    @Query("SELECT * FROM staff WHERE businessId = :businessId")
    List<Staff> getAllStaff(int businessId);

    @Query("SELECT * FROM staff WHERE id = :id")
    Staff getById(int id);
}
