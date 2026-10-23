package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.KaizenDatabase
import com.example.data.repository.KaizenRepository
import com.example.ui.KaizenApp
import com.example.ui.KaizenViewModel
import com.example.ui.theme.KaizenTheme

class MainActivity : ComponentActivity() {

    private val viewModel: KaizenViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val db = KaizenDatabase.getDatabase(applicationContext)
                val repository = KaizenRepository(db)
                return KaizenViewModel(repository) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KaizenTheme {
                KaizenApp(viewModel = viewModel)
            }
        }
    }
}
