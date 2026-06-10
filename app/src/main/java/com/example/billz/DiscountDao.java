package com.example.billz;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DiscountDao {
    @Query("SELECT * FROM discounts WHERE businessId = :businessId")
    List<Discount> getAllDiscounts(int businessId);

    @Insert
    void insert(Discount discount);

    @Update
    void update(Discount discount);

    @Delete
    void delete(Discount discount);
}
