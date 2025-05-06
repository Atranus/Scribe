package com.example.scribe

import android.os.Environment
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.nehuatl.llamacpp.LlamaHelper

class MainViewModel : ViewModel() {
    private val viewModelJob = SupervisorJob()
    private val ioScope = CoroutineScope(Dispatchers.IO + viewModelJob)
    private val llamaHelper by lazy { LlamaHelper(ioScope) }  // 9

    private val _answer = MutableStateFlow<String>("")
    val answer: StateFlow<String> = _answer.asStateFlow()

    init {
        // Автозагрузка модели при старте
        ioScope.launch {
            llamaHelper.load(
                path = "${Environment.getExternalStorageDirectory()}/Download/llama.ggmlv3.q4_0.bin",
                contextLength = 2048
            )
        }
    }

    fun submit(prompt: String) {
        ioScope.launch {
            llamaHelper.setCollector()
                .onStart {
                    _answer.emit("")  // Очистить предыдущий ответ
                }
                .onCompletion {
                    llamaHelper.unsetCollector()
                }
                .collect { chunk ->
                    // Поступающие токены по кусочкам
                    _answer.emit(_answer.value + chunk)
                }

            llamaHelper.predict(prompt = prompt, partialCompletion = true)
        }
    }

    override fun onCleared() {
        super.onCleared()
        llamaHelper.abort()
        llamaHelper.release()
        viewModelJob.cancel()
    }
}