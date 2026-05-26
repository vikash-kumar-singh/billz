package com.example.billz;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface BusinessDao {
    @Query("SELECT * FROM businesses")
    List<Business> getAllBusinesses();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Business business);

    @Update
    void update(Business business);

    @Query("UPDATE businesses SET isSelected = 0")
    void deselectAll();

    @Query("UPDATE businesses SET isSelected = 1 WHERE name = :name")
    void selectBusiness(String name);

    @Query("DELETE FROM businesses WHERE name = :name")
    void deleteByName(String name);

    @Query("SELECT * FROM businesses WHERE id = :id")
    Business getById(int id);
}
