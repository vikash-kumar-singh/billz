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
    long insert(Business business);

    @Update
    void update(Business business);

    @Query("UPDATE businesses SET isSelected = 0")
    void deselectAll();

    @Query("UPDATE businesses SET isSelected = 1 WHERE id = :id")
    void selectBusiness(int id);

    @Query("SELECT * FROM businesses WHERE isSelected = 1 LIMIT 1")
    Business getSelectedBusiness();

    @Query("DELETE FROM businesses WHERE id = :id")
    void deleteById(int id);

    @Query("SELECT * FROM businesses WHERE id = :id")
    Business getById(int id);
}
