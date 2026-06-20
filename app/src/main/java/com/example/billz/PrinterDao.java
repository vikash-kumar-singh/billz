package com.example.billz;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PrinterDao {
    @Query("SELECT * FROM printers WHERE businessId = :businessId")
    List<Printer> getAllPrinters(int businessId);

    @Insert
    void insert(Printer printer);

    @Update
    void update(Printer printer);

    @Delete
    void delete(Printer printer);

    @Query("SELECT * FROM printers WHERE address = :address AND businessId = :businessId LIMIT 1")
    Printer getByAddress(String address, int businessId);
}
