package com.example.billz;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface ReceiptDao {
    @Insert
    void insert(Receipt receipt);

    @Query("SELECT * FROM receipts WHERE businessId = :businessId ORDER BY timestamp DESC")
    List<Receipt> getReceiptsByBusiness(int businessId);

    @Query("SELECT * FROM receipts WHERE businessId = :businessId AND timestamp BETWEEN :from AND :to ORDER BY timestamp DESC")
    List<Receipt> getReceiptsByDateRange(int businessId, long from, long to);

    @Query("SELECT * FROM receipts WHERE businessId = :businessId AND paymentMode = :mode AND timestamp BETWEEN :from AND :to ORDER BY timestamp DESC")
    List<Receipt> getReceiptsByFilter(int businessId, String mode, long from, long to);
}
