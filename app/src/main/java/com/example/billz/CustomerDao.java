package com.example.billz;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CustomerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Customer customer);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Customer> customers);

    @Query("SELECT * FROM customers ORDER BY id DESC")
    List<Customer> getAllCustomers();

    @Query("SELECT * FROM customers WHERE mobile = :mobile LIMIT 1")
    Customer getCustomerByMobile(String mobile);
}
