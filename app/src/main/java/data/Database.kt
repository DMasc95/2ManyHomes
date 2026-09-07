package data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [Produto::class, Home::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract fun produtoDAO(): ProdutoDAO
    abstract fun homeDAO(): HomeDAO

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "database"
                )   .fallbackToDestructiveMigration(false) // ← apaga e recria a BD se a versão mudar
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}