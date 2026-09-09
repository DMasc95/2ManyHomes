package data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProdutoDAO {
    @Insert
    suspend fun inserir(produto: Produto)

    @Update
    suspend fun atualizar(produto: Produto)

    @Delete
    suspend fun apagar(produto: Produto)

    @Query("SELECT * FROM produtos WHERE casa = :casa ORDER BY nome ASC")
    fun getProdutosPorCasa(casa: Int): Flow<List<Produto>>

    @Query("SELECT * FROM produtos")
    fun getTodos(): Flow<List<Produto>>

    @Query("SELECT * FROM produtos WHERE casa = :casa AND tipo = :tipo ORDER BY nome ASC")
    fun getProdutosPorCasaETipo(casa: Int, tipo: TipoProduto): Flow<List<Produto>>
}