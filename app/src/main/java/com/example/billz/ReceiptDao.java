package com.example.billz;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface ReceiptDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Receipt receipt);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Receipt> receipts);

    @Update
    void update(Receipt receipt);

    @Query("SELECT * FROM receipts WHERE businessId = :businessId AND isReturned = 0 ORDER BY timestamp DESC")
    List<Receipt> getReceiptsByBusiness(int businessId);

    @Query("SELECT * FROM receipts WHERE businessId = :businessId AND isReturned = 0 AND timestamp BETWEEN :from AND :to ORDER BY timestamp DESC")
    List<Receipt> getReceiptsByDateRange(int businessId, long from, long to);

    @Query("SELECT * FROM receipts WHERE businessId = :businessId AND isReturned = 0 AND paymentMode = :mode AND timestamp BETWEEN :from AND :to ORDER BY timestamp DESC")
    List<Receipt> getReceiptsByFilter(int businessId, String mode, long from, long to);

    @Query("SELECT * FROM receipts WHERE businessId = :businessId AND isReturned = 1 ORDER BY timestamp DESC")
    List<Receipt> getReturnedReceipts(int businessId);

    @Query("SELECT * FROM receipts WHERE businessId = :businessId AND isReturned = 1 AND timestamp BETWEEN :from AND :to ORDER BY timestamp DESC")
    List<Receipt> getReturnedReceiptsByDateRange(int businessId, long from, long to);

    @Query("SELECT * FROM receipts WHERE businessId = :businessId AND isReturned = 1 AND paymentMode = :mode AND timestamp BETWEEN :from AND :to ORDER BY timestamp DESC")
    List<Receipt> getReturnedReceiptsByFilter(int businessId, String mode, long from, long to);

    @Query("SELECT * FROM receipts WHERE businessId = :businessId AND timestamp BETWEEN :from AND :to ORDER BY timestamp DESC")
    List<Receipt> getAllReceiptsByDateRange(int businessId, long from, long to);

    @Query("SELECT * FROM receipts WHERE id = :id")
    Receipt getById(String id);

    @Query("DELETE FROM receipts WHERE id = :id")
    void deleteById(String id);
}
