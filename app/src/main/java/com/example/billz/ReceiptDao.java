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
    List<Receipt> getReceiptsByBusiness(String businessId);

    @Query("SELECT * FROM receipts WHERE businessId = :businessId AND isReturned = 0 AND timestamp BETWEEN :from AND :to ORDER BY timestamp DESC")
    List<Receipt> getReceiptsByDateRange(String businessId, long from, long to);

    @Query("SELECT * FROM receipts WHERE businessId = :businessId AND isReturned = 0 AND paymentMode = :mode AND timestamp BETWEEN :from AND :to ORDER BY timestamp DESC")
    List<Receipt> getReceiptsByFilter(String businessId, String mode, long from, long to);

    @Query("SELECT * FROM receipts WHERE businessId = :businessId AND isReturned = 1 ORDER BY timestamp DESC")
    List<Receipt> getReturnedReceipts(String businessId);

    @Query("SELECT * FROM receipts WHERE businessId = :businessId AND isReturned = 1 AND updatedAt BETWEEN :from AND :to ORDER BY updatedAt DESC")
    List<Receipt> getReturnedReceiptsByDateRange(String businessId, long from, long to);

    @Query("SELECT * FROM receipts WHERE businessId = :businessId AND isReturned = 1 AND paymentMode = :mode AND updatedAt BETWEEN :from AND :to ORDER BY updatedAt DESC")
    List<Receipt> getReturnedReceiptsByFilter(String businessId, String mode, long from, long to);

    @Query("SELECT * FROM receipts WHERE businessId = :businessId AND (timestamp BETWEEN :from AND :to OR (isReturned = 1 AND updatedAt BETWEEN :from AND :to)) ORDER BY updatedAt DESC")
    List<Receipt> getAllReceiptsByDateRange(String businessId, long from, long to);

    @Query("SELECT * FROM receipts WHERE id = :id")
    Receipt getById(String id);

    @Query("SELECT * FROM receipts WHERE customerId = :customerId ORDER BY timestamp DESC")
    List<Receipt> getReceiptsByCustomer(String customerId);

    @Query("DELETE FROM receipts WHERE id = :id")
    void deleteById(String id);

    @Query("SELECT * FROM receipts WHERE syncPending = 1")
    List<Receipt> getPendingSyncReceipts();
}
