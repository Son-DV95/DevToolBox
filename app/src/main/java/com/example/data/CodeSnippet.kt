package com.example.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "code_snippets")
data class CodeSnippet(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val code: String,
    val language: String, // e.g. "Kotlin", "Java"
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface CodeSnippetDao {
    @Query("SELECT * FROM code_snippets ORDER BY timestamp DESC")
    fun getAllSnippets(): Flow<List<CodeSnippet>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnippet(snippet: CodeSnippet)

    @Query("DELETE FROM code_snippets WHERE id = :id")
    suspend fun deleteSnippetById(id: Long)
}

@Database(entities = [CodeSnippet::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun codeSnippetDao(): CodeSnippetDao
}

class CodeSnippetRepository(private val dao: CodeSnippetDao) {
    val allSnippets: Flow<List<CodeSnippet>> = dao.getAllSnippets()

    suspend fun insert(snippet: CodeSnippet) {
        dao.insertSnippet(snippet)
    }

    suspend fun deleteById(id: Long) {
        dao.deleteSnippetById(id)
    }
}
