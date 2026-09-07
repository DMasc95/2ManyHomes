package com.example.a2manyhomes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
                        composable("filtro"){
                            FiltroScreen(navController)
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

@Composable
fun ListaProdutosScreen(idCasa: Int, produtoViewModel: ProdutoViewModel, homeViewModel: HomeViewModel, navController: NavController) { //funcao que cria o novo screen
    val produtos by produtoViewModel.getProdutosPorCasa(idCasa).collectAsState(initial = emptyList())
    val casa by remember(idCasa) { homeViewModel.getHomeporId(idCasa) }.collectAsState(initial = null)

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
                    navController.navigate("filtro")
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "O que procuras?"
                )
            }
        }

        //lista para dar scroll dos produtos
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 100.dp, bottom = 100.dp)
        ) {
            items(produtos) { produto ->
                CardProduto(produto)
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
fun FiltroScreen(navController: NavController,) {
    //possivelmente vou ter de fazer algo assim para ter os filtros
    //val filtros by produtoViewModel.getProdutosPorCasa(idCasa).collectAsState(initial = emptyList())
    //possivelmente vou ter que criar ficheiros DAO novos, para criar funcoes para ir buscar os filtros


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
            .padding(top = 100.dp, bottom = 100.dp)
    ) {
        items(TipoProduto.entries) { tipo ->
            CardFiltro(tipo)
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
            //botao do adicionar produto
            Button(
                onClick = {
                    //funcao que aplica os filtros no screen da lista de produtos
                    //navController.navigate("adicionar_produto/${idCasa}")
                },
                modifier = Modifier.padding(16.dp)

            ) {
                Text(
                    text = "Aplicar filtros"
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
fun CardProduto(produto: Produto) {
    //personalizar o card de cada produto

    Card(                                 //isto e para criarmos como se fosse uma caixa a volta do texto
        modifier = Modifier
            .fillMaxSize() //maximizar o tamanho do preenchimento
            .padding(12.dp)          //criar o espacamento entre eles de 12 dp's(?)
            .clickable() {

                //MAYBE
                //se carregarmos, abre a descricao do produto e da para alterar com um + e um - a quantidade de produtos
                //MAYBE

            }
        //.pointerHoverIcon(PointerIcon.Hand) //objetivo seria ver que elemento e clicavel, mas tudo pacifico
    ) {
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
    }
}

@Composable
fun CardFiltro(tipoProduto: TipoProduto) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .clickable() {

                //ao carregar, selecionamos o filtro e mandamos para tras

            }
        ) {
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
                "${produto.tipo}",
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}



//TO DO

//personalizar o cartao para colocar o + e o - para aumentar ou diminuir a quantidade de um produto
//possibilidade de filtro
//barra de procura?



//adicionar algo para eliminar a casa caso queira