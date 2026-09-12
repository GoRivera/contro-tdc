package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.CreditCardDao
import com.example.data.dao.ExpenseDao
import com.example.data.dao.FuelEntryDao
import com.example.data.dao.PaymentDao
import com.example.data.dao.SubscriptionDao
import com.example.data.model.CreditCard
import com.example.data.model.Expense
import com.example.data.model.FuelEntry
import com.example.data.model.Payment
import com.example.data.model.Subscription
import com.example.data.model.SubscriptionPaymentTracking
import androidx.room.migration.Migration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [CreditCard::class, Expense::class, Payment::class, Subscription::class, SubscriptionPaymentTracking::class, FuelEntry::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun creditCardDao(): CreditCardDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun paymentDao(): PaymentDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun fuelEntryDao(): FuelEntryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE subscriptions ADD COLUMN periodicity TEXT NOT NULL DEFAULT 'MENSUAL'")
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "credit_cards_manager.db"
                )
                    .addMigrations(MIGRATION_4_5)
                    .fallbackToDestructiveMigration()
                    .addCallback(AppDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    InitialDataSeeder.seedDatabase(database)
                }
            }
        }
    }
}
