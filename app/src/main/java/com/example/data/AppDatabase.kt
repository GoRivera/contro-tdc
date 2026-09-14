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
import com.example.data.dao.ServiceEntryDao
import com.example.data.dao.SubscriptionDao
import com.example.data.model.CreditCard
import com.example.data.model.Expense
import com.example.data.model.FuelEntry
import com.example.data.model.Payment
import com.example.data.model.ServiceEntry
import com.example.data.model.Subscription
import com.example.data.model.SubscriptionPaymentTracking
import androidx.room.migration.Migration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [CreditCard::class, Expense::class, Payment::class, Subscription::class, SubscriptionPaymentTracking::class, FuelEntry::class, ServiceEntry::class],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun creditCardDao(): CreditCardDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun paymentDao(): PaymentDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun fuelEntryDao(): FuelEntryDao
    abstract fun serviceEntryDao(): ServiceEntryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE subscriptions ADD COLUMN periodicity TEXT NOT NULL DEFAULT 'MENSUAL'")
            }
        }

        /**
         * Agrega el identificador estable `firestoreId` (UUID) a todas las tablas sincronizables,
         * para eliminar la colisión de IDs entre dispositivos al sincronizar con la nube, además de
         * la tasa de interés real por tarjeta y el número de personas entre las que se divide una carga
         * de combustible.
         */
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE credit_cards ADD COLUMN firestoreId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE credit_cards ADD COLUMN annualInterestRatePercent REAL NOT NULL DEFAULT 55.0")
                db.execSQL("ALTER TABLE expenses ADD COLUMN firestoreId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE payments ADD COLUMN firestoreId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE subscriptions ADD COLUMN firestoreId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE subscription_payment_trackings ADD COLUMN firestoreId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE fuel_entries ADD COLUMN firestoreId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE fuel_entries ADD COLUMN dividedCount INTEGER NOT NULL DEFAULT 2")
            }
        }

        /**
         * Agrega la tabla de "Servicios" (agua, luz, gas): un módulo de control de consumos
         * totalmente independiente de las tarjetas de crédito y sus gastos.
         */
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `service_entries` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`serviceType` TEXT NOT NULL, " +
                        "`dateMillis` INTEGER NOT NULL, " +
                        "`amount` REAL NOT NULL, " +
                        "`consumption` REAL NOT NULL DEFAULT 0.0, " +
                        "`notes` TEXT NOT NULL DEFAULT '', " +
                        "`firestoreId` TEXT NOT NULL DEFAULT '')"
                )
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "credit_cards_manager.db"
                )
                    .addMigrations(MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
                    .fallbackToDestructiveMigration(dropAllTables = true)
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
