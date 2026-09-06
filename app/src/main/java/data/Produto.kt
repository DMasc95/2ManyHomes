package data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "produtos",
    foreignKeys = [
        ForeignKey(
            entity = Home::class, //tabela original
            parentColumns = ["id"], //id da tabela original
            childColumns = ["casa"], //relacionar com a chave desta tabela
            onDelete = ForeignKey.CASCADE // apaga produtos se a casa for apagada
        )
    ],
    indices = [Index("casa")] // recomendado para performance
)
data class Produto (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nome: String,
    val quantidade: Double,
    val casa: Int, // id da casa onde o alimento está guardado
    val tipo: TipoProduto
)

enum class TipoProduto {
    MERCEARIA,
    FRESCOS,
    FRUTAS,
    LEGUMES,
    ENLATADOS,
    BEBIDAS,
    UTENSILIO,
    TEMPERO,
    HIGIENE,
    LIMPEZA,
    CONGELADO,
    LIVROS,
    NENHUM
}