package viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import data.Produto
import data.ProdutoRepository
import data.TipoProduto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProdutoViewModel(private val repository: ProdutoRepository) : ViewModel() {

    fun getProdutosPorCasa(casa: Int): StateFlow<List<Produto>> =
        repository.getProdutosPorCasa(casa)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun inserir(produto: Produto) {
        viewModelScope.launch {
            repository.inserir(produto)
        }
    }

    fun atualizar(produto: Produto) {
        viewModelScope.launch {
            repository.atualizar(produto)
        }
    }

    fun apagar(produto: Produto) {
        viewModelScope.launch {
            repository.apagar(produto)
        }
    }

    fun getProdutosPorCasaETipo(casa: Int, tipo: TipoProduto): StateFlow<List<Produto>> =
        repository.getProdutosPorCasaETipo(casa,tipo)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _quantidadesCompra = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val quantidadesCompra: StateFlow<Map<Int, Int>> = _quantidadesCompra

    fun definirQuantidadeCompra(produtoId: Int, quantidade: Int) {
        _quantidadesCompra.value = _quantidadesCompra.value.toMutableMap().apply {
            this[produtoId] = quantidade
        }
    }

    fun confirmarECompras(produtos: List<Produto>) {
        viewModelScope.launch {
            produtos.forEach { produto ->
                val novaQuantidade = _quantidadesCompra.value[produto.id]
                if (novaQuantidade != null) {
                    repository.atualizar(produto.copy(quantidade = novaQuantidade))
                    _quantidadesCompra.value = _quantidadesCompra.value - produto.id
                }
            }
        }
    }
}

class ProdutoViewModelFactory(private val repository: ProdutoRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProdutoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProdutoViewModel(repository) as T
        }
        throw IllegalArgumentException("ViewModel desconhecida")
    }
}