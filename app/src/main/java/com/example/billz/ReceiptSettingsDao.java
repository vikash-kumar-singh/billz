package com.example.billz;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface ReceiptSettingsDao {
    @Query("SELECT * FROM receipt_settings WHERE id = :businessId")
    ReceiptSettings getSettingsByBusiness(int businessId);

    @Query("SELECT * FROM receipt_settings LIMIT 1")
    ReceiptSettings getSettings();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ReceiptSettings settings);

    @Update
    void update(ReceiptSettings settings);
}
