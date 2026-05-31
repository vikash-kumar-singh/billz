package com.example.billz;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface SubscriptionDao {
    @Query("SELECT * FROM subscriptions WHERE id = 1")
    Subscription getActiveSubscription();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Subscription subscription);

    @Update
    void update(Subscription subscription);

    @Query("SELECT * FROM billing_history ORDER BY purchaseDate DESC")
    List<BillingHistory> getBillingHistory();

    @Insert
    void insertBillingHistory(BillingHistory history);
}
