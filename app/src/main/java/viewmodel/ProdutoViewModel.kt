package viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import data.Produto
import data.ProdutoRepository
import data.TipoProduto
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