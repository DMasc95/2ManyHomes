package data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HomeDAO {
    @Insert
    suspend fun inserir(casa: Home)

    @Update
    suspend fun atualizar(casa: Home)

    @Delete
    suspend fun apagar(casa: Home)

    @Query("SELECT * FROM homes")
    fun getCasas(): Flow<List<Home>>

    @Query("SELECT * FROM homes WHERE id=:id")
    fun getHomeporId(id: Int): Flow<Home?>
}