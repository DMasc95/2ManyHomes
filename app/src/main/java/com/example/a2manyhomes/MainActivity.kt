package com.example.a2manyhomes

import android.os.Bundle
import android.service.autofill.OnClickAction
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.a2manyhomes.ui.theme._2ManyHomesTheme
import data.AppDatabase
import data.Home
import data.HomeRepository
import data.Produto
import data.ProdutoRepository
import data.TipoProduto
import viewmodel.HomeViewModel
import viewmodel.HomeViewModelFactory
import viewmodel.ProdutoViewModel
import viewmodel.ProdutoViewModelFactory


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        //relacionamento da base de dados com os repositorys e as factorys
        val db = AppDatabase.getDatabase(applicationContext)

        val repository = ProdutoRepository(db.produtoDAO())
        val factory = ProdutoViewModelFactory(repository)

        val homeRepository = HomeRepository(db.homeDAO())
        val homeFactory = HomeViewModelFactory(homeRepository)


        setContent {
            _2ManyHomesTheme {

                val navController = rememberNavController() //controler para mudar de screens

                //viewmodels
                val homeViewModel: HomeViewModel = viewModel(factory = homeFactory)
                val produtoviewModel: ProdutoViewModel = viewModel(factory = factory)


                //ListaCasasScreen(lista, viewModel)

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("home") {
                            val casas by homeViewModel.getCasas.collectAsState(initial = emptyList())
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                LazyColumn() { //list vertical que se ve ao dar scroll
                                    items(casas) { casa ->
                                        CardView(
                                            casa,
                                            navController,
                                            homeViewModel //isto e adicionado porque depois temos de passar a informacao de que casa estamos a falar
                                        )
                                    }
                                }
                                AddNovaCasa(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(16.dp),
                                    navController
                                )
                            }

                        }
                        composable(
                            route = "lista_coisas/{idcasa}",
                            arguments = listOf(navArgument("idcasa") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val idcasa = backStackEntry.arguments?.getInt("idcasa") ?: 0 //?: - operador Elvis — abreviatura do Kotlin para "se isto for null, usa este valor por default".
                            ListaProdutosScreen(idcasa,produtoviewModel,homeViewModel,navController)
                        }
                        composable("inserir_casa") {
                            InserirCasaScreen(homeViewModel,navController)
                        }
                        composable(
                            route ="adicionar_produto/{idcasa}",
                            arguments = listOf(navArgument("idcasa") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val idcasa = backStackEntry.arguments?.getInt("idcasa") ?: 0
                            AdicionarProdutoScreen(navController,produtoviewModel,idcasa)
                        }
                        composable(
                            route = "filtro/{idcasa}",
                            arguments = listOf(navArgument("idcasa") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val idcasa = backStackEntry.arguments?.getInt("idcasa") ?: 0
                            FiltroScreen(navController,idcasa)
                        }
                        composable(
                            route = "produtos_filtrados/{idCasa}/{tipoProduto}",
                            arguments = listOf(
                                navArgument("idCasa") { type = NavType.IntType },
                                navArgument("tipoProduto") {type = NavType.StringType}
                            )
                        ) { backStackEntry ->
                            val idCasa = backStackEntry.arguments?.getInt("idCasa") ?: 0
                            val tipoStr = backStackEntry.arguments?.getString("tipoProduto") ?: TipoProduto.OUTRO.name
                            val tipo = TipoProduto.valueOf(tipoStr)
                            ProdutosFiltradosScreen(navController,idCasa,tipo,produtoviewModel,homeViewModel)
                        }
                    }
                }
            }
        }
    }
}




@Composable
fun AddNovaCasa(modifier: Modifier = Modifier,navController: NavController) {
    Card(modifier
        .size(56.dp)
        .clickable() {
            navController.navigate("inserir_casa")
        }) {
        Box( //este box e acrescentado para controlar como fica posicionado o texto dentro do card
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text="+",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}


@Composable
fun InserirCasaScreen(homeViewModel: HomeViewModel,navController: NavController) { //ecra de inserir nova casa
    var nomeCasa by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = nomeCasa,
            onValueChange = { novoTexto ->
                nomeCasa = novoTexto.split(" ").joinToString(" ") { palavra ->
                    palavra.replaceFirstChar { it.uppercase() }
                }
            },
            label = { Text("Nome da casa") },
            modifier = Modifier.fillMaxWidth()

        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {
                    navController.popBackStack()   // volta para o ecrã anterior ("home")
                }
            ) {
                Text("Cancelar")
            }
            Button(
                onClick = {
                    homeViewModel.inserir(Home(localizacao = nomeCasa))
                    navController.popBackStack()   // volta para o ecrã anterior ("home")
                },
                enabled = nomeCasa.isNotBlank()
            ) {
                Text("Adicionar")
            }
        }
    }
}

@Composable
fun AdicionarProdutoScreen(navController: NavController,produtoViewModel: ProdutoViewModel,idCasa: Int) { //ecra de adicionar um produto novo
    var nomeProduto by remember { mutableStateOf("") }
    var quantidadeProduto by remember { mutableStateOf("") }
    var tipoSelecionado by remember { mutableStateOf(TipoProduto.OUTRO) }   // valor inicial

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            //caixa do nome do produto
            OutlinedTextField(
                value = nomeProduto,
                onValueChange = { novoTexto ->
                    nomeProduto = novoTexto.replaceFirstChar { char ->
                        if (char.isLowerCase()) char.titlecase() else char.toString()
                    }
                },
                label = { Text("Nome do Produto") },
                modifier = Modifier.fillMaxWidth()
            )
            //caixa da quantidade
            OutlinedTextField(
                value = quantidadeProduto,
                onValueChange = { quantidadeProduto = it },
                label = { Text("Qual é a quantidade") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            //dropdown do tipo do produto
            DropdownTipoProduto(
                tipoSelecionado = tipoSelecionado,
                onTipoSelecionado = { tipoSelecionado = it }
            )
        }
        //botoes para adicionar
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Button(
                    onClick = {
                        navController.popBackStack()   // volta para o ecrã anterior, ecra da lista de produtos
                    }
                ) {
                    Text("Cancelar")
                }
                Button(
                    onClick = {
                        val quantidade = quantidadeProduto.toIntOrNull() ?: 0 //isto tem de ter para tornar o numero um double, assim nao complica o textField
                        produtoViewModel.inserir(Produto(nome=nomeProduto, quantidade = quantidade, casa= idCasa, tipo = tipoSelecionado))
                        //casa e so o id da casa correspondente, portanto Int

                        navController.popBackStack()   // volta para o ecrã anterior, volta para o ecrã anterior, ecra da lista de produtos
                    },
                    enabled = nomeProduto.isNotBlank()
                ) {
                    Text("Adicionar")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaProdutosScreen(idCasa: Int, produtoViewModel: ProdutoViewModel, homeViewModel: HomeViewModel, navController: NavController) { //funcao que cria o novo screen
    val produtos by produtoViewModel.getProdutosPorCasa(idCasa).collectAsState(initial = emptyList())
    val casa by remember(idCasa) { homeViewModel.getHomeporId(idCasa) }.collectAsState(initial = null)

    var mostrarSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        //caixa do nome da localizacao em cima
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Text(
                text=casa?.localizacao ?: "A carregar...", // adiciona-se os ?, porque como casa é Home? (pode ser null), e o Kotlin não te deixa aceder a .localizacao diretamente sem tratar essa possibilidade.
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        //caixa para colocar o filtro
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Button(
                onClick = {
                    //funcao que abre um novo screen de filtro
                    navController.navigate("filtro/${idCasa}")
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "O que procuras?"
                )
            }
        }

        //lista para dar scroll dos produtos


        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomEnd,
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 100.dp, bottom = 100.dp)
            ) {
                items(produtos) { produto ->
                    CardProduto(produto,produtoViewModel)
                }
            }
            Button(
                onClick = {
                    mostrarSheet = true
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(
                        end = 16.dp,
                        bottom = 90.dp
                    )
                    .size(65.dp),
                shape = CircleShape
            ) {
                Image(
                    painter = painterResource(id = R.drawable.shopping_basket_svgrepo_com),       //definir a source da imagem
                    contentDescription = "Foto de uma lista das compras",                         //descricao para quem nao tem acesso a imagem
                    modifier = Modifier.size(800.dp)                                      //altura da imagem
                )
            }
        }

        if (mostrarSheet) { //criar algures um botao para adicionar algo a lista das compras, assim como eliminar
            ModalBottomSheet(
                onDismissRequest = { mostrarSheet = false },
                sheetState = sheetState
            ) {
                LazyColumn(modifier = Modifier.padding(16.dp)) {
                    //aqui colocar a lista das compras, com o produto desejado e a quantidade
                    items(produtos) { produto->
                        //if quantidade do produto = 0, adicionar automaticamente
                        if(produto.quantidade == 0) {
                            CardProdutoListaCompras(produto, produtoViewModel)
                        }
                    }
                }
            }
        }

        //caixa para ter o botao de recuar e adicionar produto
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                //botao para recuar
                Button(
                    onClick = {
                        navController.popBackStack()   // volta para o ecrã anterior ("home")
                    },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Recuar"
                    )
                }
                //botao do adicionar produto
                Button(
                    onClick = {
                        //funcao que abre um novo screen de adicionar produto
                        navController.navigate("adicionar_produto/${idCasa}")
                    },
                    modifier = Modifier.padding(16.dp)

                ) {
                    Text(
                        text = "Adicionar produto"
                    )
                }
            }
        }
    }
}

@Composable
fun ProdutosFiltradosScreen(navController: NavController, idcasa: Int, tipoProduto: TipoProduto, produtoViewModel: ProdutoViewModel, homeViewModel: HomeViewModel) {

    //o titulo pode ser algo como "Produto filtrado - Oeiras"
    //basicamente "produto selecionado - casa selecionada", e depois aparece a lista toda de produtos

    val produtos by remember(idcasa, tipoProduto) { produtoViewModel.getProdutosPorCasaETipo(idcasa, tipoProduto) }.collectAsState(initial = emptyList())
    val casa by remember(idcasa) { homeViewModel.getHomeporId(idcasa) }.collectAsState(initial = null)

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        //caixa do nome da localizacao em cima
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Text(
                text = "${tipoProduto.name} — ${casa?.localizacao ?:"A carregar..."}",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        //lista para dar scroll dos produtos
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 50.dp, bottom = 100.dp)
        ) {
            items(produtos) { produto ->
                CardProduto(produto,produtoViewModel)
            }
        }

        //caixa para ter o botao de recuar e adicionar produto
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                //botao para recuar
                Button(
                    onClick = {
                        navController.popBackStack()   // volta para o ecrã anterior ("home")
                    },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Recuar"
                    )
                }
            }
        }
    }


}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownTipoProduto(
    tipoSelecionado: TipoProduto,
    onTipoSelecionado: (TipoProduto) -> Unit
    ) {

    var expandido by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expandido,
        onExpandedChange = { expandido = !expandido }
    ) {
        OutlinedTextField(
            value = tipoSelecionado.name,
            onValueChange = {},
            readOnly = true,
            label = { Text("Tipo de produto") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)   // liga o campo ao menu
        )
        ExposedDropdownMenu(
            expanded = expandido,
            onDismissRequest = { expandido = false }
        ) {
            TipoProduto.entries.forEach { tipo ->
                DropdownMenuItem(
                    text = { Text(tipo.name) },
                    onClick = {
                        onTipoSelecionado(tipo)
                        expandido = false
                    }
                )
            }
        }
    }
}



@Composable
fun FiltroScreen(navController: NavController,idCasa: Int) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        //caixa do texto Filtro em cima
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Text(
                text = "O que procuras?",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
    //adicionar filtros
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 50.dp, bottom = 100.dp)
    ) {
        items(TipoProduto.entries) { tipo ->
            CardFiltro(
                tipo,
                navController,
                idCasa
            )
        }
    }
    //caixa para ter o botao de recuar e aplicar filtros
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            //botao para recuar
            Button(
                onClick = {
                    navController.popBackStack()   // volta para o ecrã anterior, a lista de produtos
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Recuar"
                )
            }
        }
    }
}





@Composable
fun CardView(casa: Home, navController: NavController, viewModel: HomeViewModel) { //cards das casas
    //viewmodel pode vir a ser necessario para o caso de eliminarmos casas
    Card(                                 //isto e para criarmos como se fosse uma caixa a volta do texto
        modifier = Modifier
            .fillMaxSize() //maximizar o tamanho do preenchimento
            .padding(12.dp)          //criar o espacamento entre eles de 12 dp's(?)
            .clickable() {
                navController.navigate("lista_coisas/${casa.id}")  //${casa.id} - tem de se colocar para sabermos exatamente que casa estamos a trabalhar
                // e a unica maneira de fazer a comunicação entre ecrãs, por argumentos de rota
            }
    ) {
        Row() {
            Image(
                painter = painterResource(id = R.drawable.baseline_home_24), //definir a source da imagem
                contentDescription = "Foto de casa",                         //descricao para quem nao tem acesso a imagem
                modifier = Modifier
                    .width(50.dp)                             //largura da imagem
                    .height(50.dp)                                           //altura da imagem
            )
            Text(
                text = casa.localizacao,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}


@Composable
fun CardProduto(produto: Produto, produtoViewModel: ProdutoViewModel) {
    //personalizar o card de cada produto
    var expandido by remember { mutableStateOf(false) } //declaracao de expandido para expandir o card no caso de um click

    Card(                                 //isto e para criarmos como se fosse uma caixa a volta do texto
        modifier = Modifier
            .fillMaxSize() //maximizar o tamanho do preenchimento
            .padding(12.dp)          //criar o espacamento entre eles de 12 dp's(?)
            .clickable() {
                expandido = !expandido //tornar o expandido true, ou false, dependendo do valor em que se encontrava
            }
            .animateContentSize()
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row() {
                //possibilidade de criar aqui um ciclo if:
                //se a descricao do produto for alimento, imagem de alimento, se for outra coisa e outra coisa
                if(produto.tipo == TipoProduto.MERCEARIA) {
                    Image(
                        painter = painterResource(id = R.drawable.pasta_svgrepo_com), //definir a source da imagem
                        contentDescription = "Foto de massa",                         //descricao para quem nao tem acesso a imagem
                        modifier = Modifier.width(50.dp)                             //largura da imagem
                            .height(50.dp)                                           //altura da imagem
                    )
                } else if(produto.tipo == TipoProduto.FRESCOS) {
                    Image(
                        painter = painterResource(id = R.drawable.yogurt_and_spoon_svgrepo_com), //definir a source da imagem
                        contentDescription = "Foto de um iogurte",                         //descricao para quem nao tem acesso a imagem
                        modifier = Modifier.width(50.dp)                             //largura da imagem
                            .height(50.dp)                                           //altura da imagem
                    )
                } else if(produto.tipo == TipoProduto.FRUTAS) {
                    Image(
                        painter = painterResource(id = R.drawable.apple_6_svgrepo_com), //definir a source da imagem
                        contentDescription = "Foto de uma maca",                         //descricao para quem nao tem acesso a imagem
                        modifier = Modifier.width(50.dp)                             //largura da imagem
                            .height(50.dp)                                           //altura da imagem
                    )
                } else if(produto.tipo == TipoProduto.LEGUMES) {
                    Image(
                        painter = painterResource(id = R.drawable.carrot_svgrepo_com), //definir a source da imagem
                        contentDescription = "Foto de cenoura",                         //descricao para quem nao tem acesso a imagem
                        modifier = Modifier.width(50.dp)                             //largura da imagem
                            .height(50.dp)                                           //altura da imagem
                    )
                } else if(produto.tipo == TipoProduto.ENLATADOS) {
                    Image(
                        painter = painterResource(id = R.drawable.sardine_tuna_svgrepo_com), //definir a source da imagem
                        contentDescription = "Foto de massa",                         //descricao para quem nao tem acesso a imagem
                        modifier = Modifier.width(50.dp)                             //largura da imagem
                            .height(50.dp)                                           //altura da imagem
                    )
                } else if(produto.tipo == TipoProduto.BEBIDAS) {
                    Image(
                        painter = painterResource(id = R.drawable.thin_bottle_of_water_svgrepo_com), //definir a source da imagem
                        contentDescription = "Foto de massa",                         //descricao para quem nao tem acesso a imagem
                        modifier = Modifier.width(50.dp)                             //largura da imagem
                            .height(50.dp)                                           //altura da imagem
                    )
                } else if(produto.tipo == TipoProduto.UTENSILIO) {
                    Image(
                        painter = painterResource(id = R.drawable.spatula_svgrepo_com), //definir a source da imagem
                        contentDescription = "Foto de massa",                         //descricao para quem nao tem acesso a imagem
                        modifier = Modifier.width(50.dp)                             //largura da imagem
                            .height(50.dp)                                           //altura da imagem
                    )
                } else if(produto.tipo == TipoProduto.TEMPERO) {
                    Image(
                        painter = painterResource(id = R.drawable.salt_and_pepper_salt_svgrepo_com), //definir a source da imagem
                        contentDescription = "Foto de massa",                         //descricao para quem nao tem acesso a imagem
                        modifier = Modifier.width(50.dp)                             //largura da imagem
                            .height(50.dp)                                           //altura da imagem
                    )
                } else if(produto.tipo == TipoProduto.HIGIENE) {
                    Image(
                        painter = painterResource(id = R.drawable.toothbrush_and_paste_svgrepo_com), //definir a source da imagem
                        contentDescription = "Foto de massa",                         //descricao para quem nao tem acesso a imagem
                        modifier = Modifier.width(50.dp)                             //largura da imagem
                            .height(50.dp)                                           //altura da imagem
                    )
                } else if(produto.tipo == TipoProduto.LIMPEZA) {
                    Image(
                        painter = painterResource(id = R.drawable.cleaning_spray_svgrepo_com), //definir a source da imagem
                        contentDescription = "Foto de massa",                         //descricao para quem nao tem acesso a imagem
                        modifier = Modifier.width(50.dp)                             //largura da imagem
                            .height(50.dp)                                           //altura da imagem
                    )
                } else if(produto.tipo == TipoProduto.CONGELADO) {
                    Image(
                        painter = painterResource(id = R.drawable.frozen_svgrepo_com), //definir a source da imagem
                        contentDescription = "Foto de massa",                         //descricao para quem nao tem acesso a imagem
                        modifier = Modifier.width(50.dp)                             //largura da imagem
                            .height(50.dp)                                           //altura da imagem
                    )
                } else if(produto.tipo == TipoProduto.LIVROS) {
                    Image(
                        painter = painterResource(id = R.drawable.books_svgrepo_com), //definir a source da imagem
                        contentDescription = "Foto de massa",                         //descricao para quem nao tem acesso a imagem
                        modifier = Modifier.width(50.dp)                             //largura da imagem
                            .height(50.dp)                                           //altura da imagem
                    )
                } else if(produto.tipo == TipoProduto.OUTRO) {
                    Image(
                        painter = painterResource(id = R.drawable.question_mark_svgrepo_com), //definir a source da imagem
                        contentDescription = "Foto de massa",                         //descricao para quem nao tem acesso a imagem
                        modifier = Modifier.width(50.dp)                             //largura da imagem
                            .height(50.dp)                                           //altura da imagem
                    )
                }
                Text(
                    "${produto.nome} — ${produto.quantidade}",
                    modifier = Modifier.padding(12.dp)
                )
            }
            var quantidadePreparada by remember(produto.id) {
                mutableIntStateOf(produto.quantidade)
            }
            if (expandido) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        if (quantidadePreparada > 0) {
                            quantidadePreparada--
                        }
                    }) {
                        Text("-", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }

                    Text(
                        text = "$quantidadePreparada",
                        fontSize = 20.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    IconButton(onClick = {
                        quantidadePreparada++
                    }) {
                        Text("+", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            produtoViewModel.atualizar(produto.copy(quantidade = quantidadePreparada))
                            expandido = !expandido
                        },
                        modifier = Modifier.padding(16.dp)

                    ) {
                        Text("Atualizar")
                    }
                }
            }
        }
    }
}


@Composable
fun CardProdutoListaCompras(produto: Produto, produtoViewModel: ProdutoViewModel) {
    var expandido by remember { mutableStateOf(false) }
    var quantidadeDesejada by remember(produto.id) {
        mutableIntStateOf(produto.quantidade)
    }
    Card(                                 //isto e para criarmos como se fosse uma caixa a volta do texto
        modifier = Modifier
            .fillMaxSize() //maximizar o tamanho do preenchimento
            .padding(12.dp)          //criar o espacamento entre eles de 12 dp's(?)
            .clickable() {
                expandido = !expandido //tornar o expandido true, ou false, dependendo do valor em que se encontrava
            }
            .animateContentSize()
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row() {
                //possibilidade de criar aqui um ciclo if:
                //se a descricao do produto for alimento, imagem de alimento, se for outra coisa e outra coisa
                if(produto.tipo == TipoProduto.MERCEARIA) {
                    Image(
                        painter = painterResource(id = R.drawable.pasta_svgrepo_com), //definir a source da imagem
                        contentDescription = "Foto de massa",                         //descricao para quem nao tem acesso a imagem
                        modifier = Modifier.width(50.dp)                             //largura da imagem
                            .height(50.dp)                                           //altura da imagem
                    )
                } else if(produto.tipo == TipoProduto.FRESCOS) {
                    Image(
                        painter = painterResource(id = R.drawable.yogurt_and_spoon_svgrepo_com), //definir a source da imagem
                        contentDescription = "Foto de um iogurte",                         //descricao para quem nao tem acesso a imagem
                        modifier = Modifier.width(50.dp)                             //largura da imagem
                            .height(50.dp)                                           //altura da imagem
                    )
                } else if(produto.tipo == TipoProduto.FRUTAS) {
                    Image(
                        painter = painterResource(id = R.drawable.apple_6_svgrepo_com), //definir a source da imagem
                        contentDescription = "Foto de uma maca",                         //descricao para quem nao tem acesso a imagem
                        modifier = Modifier.width(50.dp)                             //largura da imagem
                            .height(50.dp)                                           //altura da imagem
                    )
                } else if(produto.tipo == TipoProduto.LEGUMES) {
                    Image(
                        painter = painterResource(id = R.drawable.carrot_svgrepo_com), //definir a source da imagem
                        contentDescription = "Foto de cenoura",                         //descricao para quem nao tem acesso a imagem
                        modifier = Modifier.width(50.dp)                             //largura da imagem
                            .height(50.dp)                                           //altura da imagem
                    )
                } else if(produto.tipo == TipoProduto.ENLATADOS) {
                    Image(
                        painter = painterResource(id = R.drawable.sardine_tuna_svgrepo_com), //definir a source da imagem
                        contentDescription = "Foto de massa",                         //descricao para quem nao tem acesso a imagem
                        modifier = Modifier.width(50.dp)                             //largura da imagem
                            .height(50.dp)                                           //altura da imagem
                    )
                } else if(produto.tipo == TipoProduto.BEBIDAS) {
                    Image(
                        painter = painterResource(id = R.drawable.thin_bottle_of_water_svgrepo_com), //definir a source da imagem
                        contentDescription = "Foto de massa",                         //descricao para quem nao tem acesso a imagem
                        modifier = Modifier.width(50.dp)                             //largura da imagem
                            .height(50.dp)                                           //altura da imagem
                    )
                } else if(produto.tipo == TipoProduto.UTENSILIO) {
                    Image(
                        painter = painterResource(id = R.drawable.spatula_svgrepo_com), //definir a source da imagem
                        contentDescription = "Foto de massa",                         //descricao para quem nao tem acesso a imagem
                        modifier = Modifier.width(50.dp)                             //largura da imagem
                            .height(50.dp)                                           //altura da imagem
                    )
                } else if(produto.tipo == TipoProduto.TEMPERO) {
                    Image(
                        painter = painterResource(id = R.drawable.salt_and_pepper_salt_svgrepo_com), //definir a source da imagem
                        contentDescription = "Foto de massa",                         //descricao para quem nao tem acesso a imagem
                        modifier = Modifier.width(50.dp)                             //largura da imagem
                            .height(50.dp)                                           //altura da imagem
                    )
                } else if(produto.tipo == TipoProduto.HIGIENE) {
                    Image(
                        painter = painterResource(id = R.drawable.toothbrush_and_paste_svgrepo_com), //definir a source da imagem
                        contentDescription = "Foto de massa",                         //descricao para quem nao tem acesso a imagem
                        modifier = Modifier.width(50.dp)                             //largura da imagem
                            .height(50.dp)                                           //altura da imagem
                    )
                } else if(produto.tipo == TipoProduto.LIMPEZA) {
                    Image(
                        painter = painterResource(id = R.drawable.cleaning_spray_svgrepo_com), //definir a source da imagem
                        contentDescription = "Foto de massa",                         //descricao para quem nao tem acesso a imagem
                        modifier = Modifier.width(50.dp)                             //largura da imagem
                            .height(50.dp)                                           //altura da imagem
                    )
                } else if(produto.tipo == TipoProduto.CONGELADO) {
                    Image(
                        painter = painterResource(id = R.drawable.frozen_svgrepo_com), //definir a source da imagem
                        contentDescription = "Foto de massa",                         //descricao para quem nao tem acesso a imagem
                        modifier = Modifier.width(50.dp)                             //largura da imagem
                            .height(50.dp)                                           //altura da imagem
                    )
                } else if(produto.tipo == TipoProduto.LIVROS) {
                    Image(
                        painter = painterResource(id = R.drawable.books_svgrepo_com), //definir a source da imagem
                        contentDescription = "Foto de massa",                         //descricao para quem nao tem acesso a imagem
                        modifier = Modifier.width(50.dp)                             //largura da imagem
                            .height(50.dp)                                           //altura da imagem
                    )
                } else if(produto.tipo == TipoProduto.OUTRO) {
                    Image(
                        painter = painterResource(id = R.drawable.question_mark_svgrepo_com), //definir a source da imagem
                        contentDescription = "Foto de massa",                         //descricao para quem nao tem acesso a imagem
                        modifier = Modifier.width(50.dp)                             //largura da imagem
                            .height(50.dp)                                           //altura da imagem
                    )
                }
                Text(
                    "${produto.nome} — $quantidadeDesejada",
                    modifier = Modifier.padding(12.dp)
                )
            }

            if (expandido) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        if (quantidadeDesejada > 0) {
                            quantidadeDesejada--
                        }
                    }) {
                        Text("-", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }

                    Text(
                        text = "$quantidadeDesejada",
                        fontSize = 20.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    IconButton(onClick = {
                        quantidadeDesejada++
                    }) {
                        Text("+", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CardFiltro(tipoProduto: TipoProduto, navController: NavController, idCasa: Int) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .clickable() {
                //ao carregar, selecionamos o filtro e mandamos para tras
                navController.navigate("produtos_filtrados/${idCasa}/${tipoProduto.name}")
            }
        ) {
        Row() {
            //possibilidade de criar aqui um ciclo if:
            //se a descricao do produto for alimento, imagem de alimento, se for outra coisa e outra coisa
            if(tipoProduto == TipoProduto.MERCEARIA) {
                Image(
                    painter = painterResource(id = R.drawable.pasta_svgrepo_com), //definir a source da imagem
                    contentDescription = "Foto de massa",                         //descricao para quem nao tem acesso a imagem
                    modifier = Modifier.width(50.dp)                             //largura da imagem
                        .height(50.dp)                                           //altura da imagem
                )
            } else if(tipoProduto == TipoProduto.FRESCOS) {
                Image(
                    painter = painterResource(id = R.drawable.yogurt_and_spoon_svgrepo_com), //definir a source da imagem
                    contentDescription = "Foto de um iogurte",                         //descricao para quem nao tem acesso a imagem
                    modifier = Modifier.width(50.dp)                             //largura da imagem
                        .height(50.dp)                                           //altura da imagem
                )
            } else if(tipoProduto == TipoProduto.FRUTAS) {
                Image(
                    painter = painterResource(id = R.drawable.apple_6_svgrepo_com), //definir a source da imagem
                    contentDescription = "Foto de uma maca",                         //descricao para quem nao tem acesso a imagem
                    modifier = Modifier.width(50.dp)                             //largura da imagem
                        .height(50.dp)                                           //altura da imagem
                )
            } else if(tipoProduto == TipoProduto.LEGUMES) {
                Image(
                    painter = painterResource(id = R.drawable.carrot_svgrepo_com), //definir a source da imagem
                    contentDescription = "Foto de cenoura",                         //descricao para quem nao tem acesso a imagem
                    modifier = Modifier.width(50.dp)                             //largura da imagem
                        .height(50.dp)                                           //altura da imagem
                )
            } else if(tipoProduto == TipoProduto.ENLATADOS) {
                Image(
                    painter = painterResource(id = R.drawable.sardine_tuna_svgrepo_com), //definir a source da imagem
                    contentDescription = "Foto de massa",                         //descricao para quem nao tem acesso a imagem
                    modifier = Modifier.width(50.dp)                             //largura da imagem
                        .height(50.dp)                                           //altura da imagem
                )
            } else if(tipoProduto == TipoProduto.BEBIDAS) {
                Image(
                    painter = painterResource(id = R.drawable.thin_bottle_of_water_svgrepo_com), //definir a source da imagem
                    contentDescription = "Foto de massa",                         //descricao para quem nao tem acesso a imagem
                    modifier = Modifier.width(50.dp)                             //largura da imagem
                        .height(50.dp)                                           //altura da imagem
                )
            } else if(tipoProduto == TipoProduto.UTENSILIO) {
                Image(
                    painter = painterResource(id = R.drawable.spatula_svgrepo_com), //definir a source da imagem
                    contentDescription = "Foto de massa",                         //descricao para quem nao tem acesso a imagem
                    modifier = Modifier.width(50.dp)                             //largura da imagem
                        .height(50.dp)                                           //altura da imagem
                )
            } else if(tipoProduto == TipoProduto.TEMPERO) {
                Image(
                    painter = painterResource(id = R.drawable.salt_and_pepper_salt_svgrepo_com), //definir a source da imagem
                    contentDescription = "Foto de massa",                         //descricao para quem nao tem acesso a imagem
                    modifier = Modifier.width(50.dp)                             //largura da imagem
                        .height(50.dp)                                           //altura da imagem
                )
            } else if(tipoProduto == TipoProduto.HIGIENE) {
                Image(
                    painter = painterResource(id = R.drawable.toothbrush_and_paste_svgrepo_com), //definir a source da imagem
                    contentDescription = "Foto de massa",                         //descricao para quem nao tem acesso a imagem
                    modifier = Modifier.width(50.dp)                             //largura da imagem
                        .height(50.dp)                                           //altura da imagem
                )
            } else if(tipoProduto == TipoProduto.LIMPEZA) {
                Image(
                    painter = painterResource(id = R.drawable.cleaning_spray_svgrepo_com), //definir a source da imagem
                    contentDescription = "Foto de massa",                         //descricao para quem nao tem acesso a imagem
                    modifier = Modifier.width(50.dp)                             //largura da imagem
                        .height(50.dp)                                           //altura da imagem
                )
            } else if(tipoProduto == TipoProduto.CONGELADO) {
                Image(
                    painter = painterResource(id = R.drawable.frozen_svgrepo_com), //definir a source da imagem
                    contentDescription = "Foto de massa",                         //descricao para quem nao tem acesso a imagem
                    modifier = Modifier.width(50.dp)                             //largura da imagem
                        .height(50.dp)                                           //altura da imagem
                )
            } else if(tipoProduto == TipoProduto.LIVROS) {
                Image(
                    painter = painterResource(id = R.drawable.books_svgrepo_com), //definir a source da imagem
                    contentDescription = "Foto de massa",                         //descricao para quem nao tem acesso a imagem
                    modifier = Modifier.width(50.dp)                             //largura da imagem
                        .height(50.dp)                                           //altura da imagem
                )
            } else if(tipoProduto == TipoProduto.OUTRO) {
                Image(
                    painter = painterResource(id = R.drawable.question_mark_svgrepo_com), //definir a source da imagem
                    contentDescription = "Foto de massa",                         //descricao para quem nao tem acesso a imagem
                    modifier = Modifier.width(50.dp)                             //largura da imagem
                        .height(50.dp)                                           //altura da imagem
                )
            }
            Text(
                text = "$tipoProduto",
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}



//TO DO

//lista de compras
//colocar a lista por ordem alfabetica? se calhar e mais facil


//barra de procura?



//adicionar algo para eliminar a casa caso queira