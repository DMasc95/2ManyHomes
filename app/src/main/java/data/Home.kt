package data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "homes")
data class Home(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val localizacao: String,
)
