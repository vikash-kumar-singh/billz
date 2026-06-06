package com.example.billz;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface VariantDao {
    @Insert
    void insert(Variant variant);

    @Update
    void update(Variant variant);

    @Delete
    void delete(Variant variant);

    @Query("SELECT * FROM variants WHERE itemId = :itemId ORDER BY sortOrder ASC")
    List<Variant> getVariantsForItem(int itemId);

    @Query("DELETE FROM variants WHERE itemId = :itemId")
    void deleteVariantsForItem(int itemId);

    @Query("SELECT * FROM variants WHERE id = :id")
    Variant getById(int id);
}
