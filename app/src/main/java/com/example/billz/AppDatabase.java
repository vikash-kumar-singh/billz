package com.example.billz;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Customer.class, ReceiptSettings.class, Staff.class, Tax.class, Discount.class, PaymentMode.class, DeliveryFee.class, PackingFee.class, ServiceFee.class, OtherFee.class, UpiConfig.class, Business.class, Category.class, ModifierSet.class, ModifierOption.class, Ingredient.class, StockHistory.class, Item.class, Variant.class}, version = 42)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    public abstract CustomerDao customerDao();
    public abstract ReceiptSettingsDao receiptSettingsDao();
    public abstract StaffDao staffDao();
    public abstract TaxDao taxDao();
    public abstract DiscountDao discountDao();
    public abstract PaymentModeDao paymentModeDao();
    public abstract DeliveryFeeDao deliveryFeeDao();
    public abstract PackingFeeDao packingFeeDao();
    public abstract ServiceFeeDao serviceFeeDao();
    public abstract OtherFeeDao otherFeeDao();
    public abstract UpiConfigDao upiConfigDao();
    public abstract BusinessDao businessDao();
    public abstract CategoryDao categoryDao();
    public abstract ModifierDao modifierDao();
    public abstract IngredientDao ingredientDao();
    public abstract StockHistoryDao stockHistoryDao();
    public abstract ItemDao itemDao();
    public abstract VariantDao variantDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "billz_database")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries() // For simplicity in this example
                    .build();
        }
        return instance;
    }
}
