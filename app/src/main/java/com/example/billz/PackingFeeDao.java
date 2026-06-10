package com.example.billz;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PackingFeeDao {
    @Query("SELECT * FROM packing_fees WHERE businessId = :businessId")
    List<PackingFee> getAllPackingFees(int businessId);

    @Insert
    void insert(PackingFee fee);

    @Update
    void update(PackingFee fee);

    @Delete
    void delete(PackingFee fee);
}
