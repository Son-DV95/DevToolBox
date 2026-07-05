package com.example.data

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    private var database: AppDatabase? = null
    private var repository: CodeSnippetRepository? = null

    fun getDatabase(context: Context): AppDatabase {
        return database ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "devtoolbox_database"
            ).fallbackToDestructiveMigration().build()
            database = instance
            instance
        }
    }

    fun getRepository(context: Context): CodeSnippetRepository {
        return repository ?: synchronized(this) {
            val repo = CodeSnippetRepository(getDatabase(context).codeSnippetDao())
            repository = repo
            repo
        }
    }
}
