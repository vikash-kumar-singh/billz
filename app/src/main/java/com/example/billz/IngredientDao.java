package com.example.billz;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface IngredientDao {
    @Query("SELECT * FROM ingredients ORDER BY id DESC")
    List<Ingredient> getAllIngredients();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Ingredient ingredient);

    @Query("DELETE FROM ingredients WHERE id = :id")
    void deleteById(int id);
}
