package com.example.billz;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CategoryDao {
    @Query("SELECT * FROM categories WHERE businessId = :businessId ORDER BY id DESC")
    List<Category> getAllCategories(int businessId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Category category);

    @Query("DELETE FROM categories WHERE name = :name AND businessId = :businessId")
    void deleteByName(String name, int businessId);

    @Query("SELECT * FROM categories WHERE name = :name AND businessId = :businessId LIMIT 1")
    Category getByName(String name, int businessId);
}
