package com.example.billz;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface DeliveryFeeDao {
    @Query("SELECT * FROM delivery_fees")
    List<DeliveryFee> getAllDeliveryFees();

    @Insert
    void insert(DeliveryFee fee);

    @Update
    void update(DeliveryFee fee);

    @Delete
    void delete(DeliveryFee fee);
}
