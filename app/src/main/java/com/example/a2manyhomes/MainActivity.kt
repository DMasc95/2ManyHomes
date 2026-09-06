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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.rememberNavController
import com.example.a2manyhomes.ui.theme._2ManyHomesTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import data.AppDatabase
import data.Home
import data.HomeRepository
import data.ProdutoRepository
import viewmodel.ProdutoViewModel
import viewmodel.ProdutoViewModelFactory
import data.Produto
import viewmodel.HomeViewModel
import viewmodel.HomeViewModelFactory
import kotlin.collections.emptyList

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
                        composable("adicionar_produto") {
                            AdicionarProdutoScreen(navController,produtoviewModel)
                        }
                    }
                }
            }
        }
    }
}




@Composable
fun AddNovaCasa(modifier: Modifier = Modifier,navController: NavController) {
    Card(modifier.size(56.dp)
        .clickable(){
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
            onValueChange = { nomeCasa = it },
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
fun AdicionarProdutoScreen(navController: NavController,produtoViewModel: ProdutoViewModel) { //ecra de adicionar um produto novo
    var nomeProduto by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = nomeProduto,
            onValueChange = { nomeProduto = it },
            label = { Text("Nome do Produto") },
            modifier = Modifier.fillMaxWidth()

        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
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

                    //ARRANJAR ISTO
                    //produtoViewModel.inserir()


                    navController.popBackStack()   // volta para o ecrã anterior, volta para o ecrã anterior, ecra da lista de produtos
                },
                enabled = nomeProduto.isNotBlank()
            ) {
                Text("Adicionar")
            }
        }
    }
}

@Composable
fun ListaProdutosScreen(idCasa: Int, produtoViewModel: ProdutoViewModel, homeViewModel: HomeViewModel, navController: NavController) { //funcao que cria o novo screen
    val produtos by produtoViewModel.getProdutosPorCasa(idCasa).collectAsState(initial = emptyList())
    val casa by homeViewModel.getHomeporId(idCasa).collectAsState(initial = null)

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

    //caixa do adicionar produto
    Box(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Button(
            onClick = {
                //funcao que abre um novo screen de adicionar produto
                navController.navigate("adicionar_produto")
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Adicionar produto"
            )
        }
    }

    //lista para dar scroll dos produtos
    LazyColumn {
        items(produtos) { produto ->
            CardProduto(produto)
        }
    }

    //caixa para ter o botao de recuar
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomStart
    ) {
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





@Composable
fun CardView(casa: Home, navController: NavController, viewModel: HomeViewModel) { //cards das casas
    Card(                                 //isto e para criarmos como se fosse uma caixa a volta do texto
        modifier = Modifier.fillMaxSize() //maximizar o tamanho do preenchimento
            .padding(12.dp)          //criar o espacamento entre eles de 12 dp's(?)
            .clickable(){
                navController.navigate("lista_coisas/${casa.id}")  //${casa.id} - tem de se colocar para sabermos exatamente que casa estamos a trabalhar
                                                                          // e a unica maneira de fazer a comunicação entre ecrãs, por argumentos de rota
            }
    ) {
        Row() {
            Image(
                painter = painterResource(id = R.drawable.baseline_home_24), //definir a source da imagem
                contentDescription = "Foto de casa",                         //descricao para quem nao tem acesso a imagem
                modifier = Modifier.width(50.dp)                             //largura da imagem
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
        modifier = Modifier.fillMaxSize() //maximizar o tamanho do preenchimento
            .padding(12.dp)          //criar o espacamento entre eles de 12 dp's(?)
            .clickable(){

                //MAYBE
                //se carregarmos, abre a descricao do produto e da para alterar com um + e um - a quantidade de produtos
                //MAYBE

            }
        //.pointerHoverIcon(PointerIcon.Hand) //objetivo seria ver que elemento e clicavel, mas tudo pacifico
    ) {
        Row() {
            //possibilidade de criar aqui um ciclo if:
            //se a descricao do produto for alimento, imagem de alimento, se for outra coisa e outra coisa
            //Image(
            //    painter = painterResource(id = R.drawable.baseline_home_24), //definir a source da imagem
            //    contentDescription = "Foto de casa",                         //descricao para quem nao tem acesso a imagem
            //    modifier = Modifier.width(50.dp)                             //largura da imagem
             //       .height(50.dp)                                           //altura da imagem
            //)
            Text(
                "${produto.nome} — ${produto.quantidade}",
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}





//TO DO

//dar fix a funcao adicionarProdutoScreen
//adicionar mais coisas ao ecra, como definir quantidades, e uma dropbox com os tipos de produtos que pode ser
//criar o novo ecra para adicionar o produto
//adicionar produtos e que eles aparecam no ecra dos produtos



//personalizar o cartao para colocar o + e o - para aumentar ou diminuir a quantidade de um produto

//barra de procura?
//definir a classe produto para saber se temos de ter uma descricao do produto dentro de possiveis escolhas, tipo alimento, roupa, produto da roupa e assim
//se a descricao do produto for alimento, imagem de alimento, se for outra coisa e outra coisa

//possibilidade de filtro

//adicionar algo para eliminar a casa caso queira