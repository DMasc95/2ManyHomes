package data

import kotlinx.coroutines.flow.Flow

class HomeRepository(private val dao: HomeDAO) {
    fun getCasas(): Flow<List<Home>> = dao.getCasas()

    suspend fun inserir(home: Home) = dao.inserir(home)
    suspend fun atualizar(home: Home) = dao.atualizar(home)
    suspend fun apagar(home: Home) = dao.apagar(home)

    fun getHomeporId(id: Int): Flow<Home?> = dao.getHomeporId(id)
}