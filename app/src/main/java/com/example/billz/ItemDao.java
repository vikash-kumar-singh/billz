package com.example.billz;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ItemDao {
    @Query("SELECT * FROM items")
    List<Item> getAllItems();

    @Insert
    long insert(Item item);

    @Update
    void update(Item item);

    @Query("SELECT * FROM items WHERE id = :id")
    Item getById(int id);

    @Query("SELECT COUNT(*) FROM items WHERE category = :categoryName")
    int getItemCountByCategory(String categoryName);

    @Query("SELECT * FROM items WHERE category = :categoryName")
    List<Item> getItemsByCategory(String categoryName);

    @Query("DELETE FROM items WHERE id = :id")
    void deleteById(int id);
}
