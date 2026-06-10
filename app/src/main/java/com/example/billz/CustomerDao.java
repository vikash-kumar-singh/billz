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

    @Query("SELECT * FROM customers WHERE businessId = :businessId ORDER BY id DESC")
    List<Customer> getAllCustomers(int businessId);

    @Query("SELECT * FROM customers WHERE mobile = :mobile AND businessId = :businessId LIMIT 1")
    Customer getCustomerByMobile(String mobile, int businessId);
}
