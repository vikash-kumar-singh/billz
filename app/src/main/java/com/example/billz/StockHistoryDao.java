package com.example.billz;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface StockHistoryDao {
    @Query("SELECT * FROM stock_history WHERE ingredientId = :ingredientId ORDER BY timestamp DESC")
    List<StockHistory> getHistoryForIngredient(int ingredientId);

    @Insert
    void insert(StockHistory history);

    @Query("DELETE FROM stock_history WHERE ingredientId = :ingredientId")
    void deleteHistoryForIngredient(int ingredientId);
}
