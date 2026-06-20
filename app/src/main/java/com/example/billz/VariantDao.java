package com.example.billz;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface VariantDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Variant variant);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Variant> variants);

    @Update
    void update(Variant variant);

    @Delete
    void delete(Variant variant);

    @Query("SELECT * FROM variants WHERE itemId = :itemId ORDER BY sortOrder ASC")
    List<Variant> getVariantsForItem(String itemId);

    @Query("DELETE FROM variants WHERE itemId = :itemId")
    void deleteVariantsForItem(String itemId);

    @Query("DELETE FROM variants WHERE itemId IN (SELECT id FROM items WHERE businessId = :businessId)")
    void deleteVariantsByBusiness(int businessId);

    @Query("SELECT * FROM variants WHERE itemId = :itemId AND name = :name LIMIT 1")
    Variant getByName(String itemId, String name);

    @Query("SELECT * FROM variants WHERE id = :id")
    Variant getById(String id);
}
