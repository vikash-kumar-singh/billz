package com.example.billz;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import java.util.List;

@Dao
public interface ModifierDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertModifierSet(ModifierSet set);

    @Insert
    void insertModifierOptions(List<ModifierOption> options);

    @Query("SELECT * FROM modifier_sets WHERE businessId = :businessId ORDER BY id DESC")
    List<ModifierSet> getAllModifierSets(int businessId);

    @Query("SELECT * FROM modifier_options WHERE modifierSetId = :setId")
    List<ModifierOption> getOptionsForSet(int setId);

    @Query("DELETE FROM modifier_sets WHERE id = :setId")
    void deleteModifierSet(int setId);

    @Query("DELETE FROM modifier_options WHERE modifierSetId = :setId")
    void deleteOptionsForSet(int setId);

    @Transaction
    default void deleteFullSet(int setId) {
        deleteOptionsForSet(setId);
        deleteModifierSet(setId);
    }
}
