package com.example.scribe

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.scribe.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val vm: MainViewModel by viewModels()  // ViewModelScope внутри

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        var binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Наблюдаем за ответом
        lifecycleScope.launchWhenStarted {
            vm.answer.collect { text ->
                binding.tvAnswer.text = text
            }
        }

        binding.btnSend.setOnClickListener {
            val question = binding.etQuestion.text.toString().trim()
            if (question.isNotEmpty()) {
                vm.submit(question)  // Вызов корутины в Dispatchers.IO 10
            }
        }
    }
}