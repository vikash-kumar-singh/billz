package com.example.billz;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface UpiConfigDao {
    @Query("SELECT * FROM upi_config WHERE id = 1")
    UpiConfig getUpiConfig();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(UpiConfig config);
}
