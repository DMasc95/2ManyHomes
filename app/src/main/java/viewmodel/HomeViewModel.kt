package viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import data.Home
import data.HomeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: HomeRepository) : ViewModel() {

    val getCasas: StateFlow<List<Home>> =
        repository.getCasas()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun inserir(home: Home) {
        viewModelScope.launch {
            repository.inserir(home)
        }
    }

    fun atualizar(home: Home) {
        viewModelScope.launch {
            repository.atualizar(home)
        }
    }

    fun apagar(home: Home) {
        viewModelScope.launch {
            repository.apagar(home)
        }
    }

    fun getHomeporId(id: Int): StateFlow<Home?> =
        repository.getHomeporId(id)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}

class HomeViewModelFactory(private val repository: HomeRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository) as T
        }
        throw IllegalArgumentException("ViewModel desconhecida")
    }
}