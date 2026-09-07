package data

import kotlinx.coroutines.flow.Flow

class ProdutoRepository(private val dao: ProdutoDAO) {
    fun getProdutosPorCasa(casa: Int): Flow<List<Produto>> = dao.getProdutosPorCasa(casa)

    suspend fun inserir(produto: Produto) = dao.inserir(produto)
    suspend fun atualizar(produto: Produto) = dao.atualizar(produto)
    suspend fun apagar(produto: Produto) = dao.apagar(produto)

    fun getProdutosPorCasaETipo(casa: Int, tipo: TipoProduto): Flow<List<Produto>> = dao.getProdutosPorCasaETipo(casa,tipo)
}