package com.example.billz;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface OtherFeeDao {
    @Query("SELECT * FROM other_fees WHERE businessId = :businessId")
    List<OtherFee> getAllOtherFees(int businessId);

    @Insert
    void insert(OtherFee fee);

    @Update
    void update(OtherFee fee);

    @Delete
    void delete(OtherFee fee);
}
