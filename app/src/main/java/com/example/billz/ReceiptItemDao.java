package com.example.billz;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface ReceiptItemDao {
    @Insert
    void insertAll(List<ReceiptItem> items);

    @Update
    void update(ReceiptItem item);

    @Query("SELECT * FROM receipt_items WHERE receiptId = :receiptId")
    List<ReceiptItem> getItemsForReceipt(String receiptId);

    @Query("DELETE FROM receipt_items WHERE receiptId = :receiptId")
    void deleteByReceiptId(String receiptId);
}
