package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.db.dao.PharmaDao
import com.example.data.db.entities.BuyRequestEntity
import com.example.data.db.entities.CartItemEntity
import com.example.data.db.entities.ChatMessageEntity
import com.example.data.db.entities.MasterMedicineEntity
import com.example.data.db.entities.OfferListingEntity
import com.example.data.db.entities.PriceThresholdAlertEntity
import com.example.data.db.entities.ShopProfileEntity
import com.example.data.db.entities.TriggeredPriceAlertEntity
import com.example.data.db.entities.WatchlistItemEntity

@Database(
    entities = [
        MasterMedicineEntity::class,
        OfferListingEntity::class,
        CartItemEntity::class,
        BuyRequestEntity::class,
        ChatMessageEntity::class,
        ShopProfileEntity::class,
        WatchlistItemEntity::class,
        PriceThresholdAlertEntity::class,
        TriggeredPriceAlertEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class PharmaDatabase : RoomDatabase() {
    abstract fun pharmaDao(): PharmaDao

    companion object {
        @Volatile
        private var INSTANCE: PharmaDatabase? = null

        fun getDatabase(context: Context): PharmaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PharmaDatabase::class.java,
                    "pharma_bazaar_db"
                ).fallbackToDestructiveMigration(true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
