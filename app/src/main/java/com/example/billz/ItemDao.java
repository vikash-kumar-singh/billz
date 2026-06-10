package com.example.billz;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ItemDao {
    @Query("SELECT * FROM items WHERE businessId = :businessId")
    List<Item> getAllItems(int businessId);

    @Insert
    long insert(Item item);

    @Update
    void update(Item item);

    @Query("SELECT * FROM items WHERE id = :id")
    Item getById(int id);

    @Query("SELECT COUNT(*) FROM items WHERE category = :categoryName AND businessId = :businessId")
    int getItemCountByCategory(String categoryName, int businessId);

    @Query("SELECT * FROM items WHERE category = :categoryName AND businessId = :businessId")
    List<Item> getItemsByCategory(String categoryName, int businessId);

    @Query("SELECT COUNT(*) FROM items WHERE (category IS NULL OR category = '' OR category = 'Uncategorized' OR category = 'No Category') AND businessId = :businessId")
    int getUncategorizedItemCount(int businessId);

    @Query("SELECT * FROM items WHERE (category IS NULL OR category = '' OR category = 'Uncategorized' OR category = 'No Category') AND businessId = :businessId")
    List<Item> getUncategorizedItems(int businessId);

    @Query("DELETE FROM items WHERE id = :id")
    void deleteById(int id);
}
