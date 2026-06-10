package com.example.billz;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ServiceFeeDao {
    @Query("SELECT * FROM service_fees WHERE businessId = :businessId")
    List<ServiceFee> getAllServiceFees(int businessId);

    @Insert
    void insert(ServiceFee fee);

    @Update
    void update(ServiceFee fee);

    @Delete
    void delete(ServiceFee fee);
}
