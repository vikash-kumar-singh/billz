package com.example.billz;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ItemDao {
    @Query("SELECT * FROM items WHERE businessId = :businessId")
    List<Item> getAllItems(int businessId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Item item);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Item> items);

    @Update
    void update(Item item);

    @Query("SELECT * FROM items WHERE id = :id")
    Item getById(String id);

    @Query("SELECT COUNT(*) FROM items WHERE category = :categoryName AND businessId = :businessId")
    int getItemCountByCategory(String categoryName, int businessId);

    @Query("SELECT * FROM items WHERE category = :categoryName AND businessId = :businessId")
    List<Item> getItemsByCategory(String categoryName, int businessId);

    @Query("SELECT COUNT(*) FROM items WHERE (category IS NULL OR category = '' OR category = 'Uncategorized' OR category = 'No Category') AND businessId = :businessId")
    int getUncategorizedItemCount(int businessId);

    @Query("SELECT * FROM items WHERE (category IS NULL OR category = '' OR category = 'Uncategorized' OR category = 'No Category') AND businessId = :businessId")
    List<Item> getUncategorizedItems(int businessId);

    @Query("SELECT * FROM items WHERE category IS NOT NULL AND category != '' AND category != 'Uncategorized' AND category != 'No Category' AND businessId = :businessId")
    List<Item> getCategorizedItems(int businessId);

    @Query("SELECT * FROM items WHERE name = :name AND businessId = :businessId LIMIT 1")
    Item getByName(String name, int businessId);

    @Query("DELETE FROM items WHERE id = :id")
    void deleteById(String id);

    @Query("DELETE FROM items WHERE businessId = :businessId")
    void deleteItemsByBusiness(int businessId);
}
