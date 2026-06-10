package com.example.billz;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PaymentModeDao {
    @Query("SELECT * FROM payment_modes WHERE businessId = :businessId")
    List<PaymentMode> getAllPaymentModes(int businessId);

    @Insert
    void insert(PaymentMode mode);

    @Update
    void update(PaymentMode mode);

    @Delete
    void delete(PaymentMode mode);
}
