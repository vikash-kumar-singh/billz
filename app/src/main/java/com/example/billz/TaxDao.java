package com.example.billz;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TaxDao {
    @Query("SELECT * FROM taxes WHERE businessId = :businessId")
    List<Tax> getAllTaxes(int businessId);

    @Insert
    void insert(Tax tax);

    @Update
    void update(Tax tax);

    @Delete
    void delete(Tax tax);
}
