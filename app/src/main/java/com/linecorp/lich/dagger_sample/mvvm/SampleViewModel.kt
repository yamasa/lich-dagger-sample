/*
 * Copyright 2019 LINE Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.linecorp.lich.dagger_sample.mvvm

import android.content.Context
import android.view.View
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.linecorp.lich.component.getComponent
import com.linecorp.lich.dagger_sample.ApplicationGraph
import com.linecorp.lich.dagger_sample.R
import com.linecorp.lich.viewmodel.AbstractViewModel
import com.linecorp.lich.viewmodel.Argument
import com.linecorp.lich.viewmodel.GenerateArgs
import com.linecorp.lich.viewmodel.SavedState
import com.linecorp.lich.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch
import javax.inject.Inject

@GenerateArgs
@MainThread
class SampleViewModel @Inject constructor(
    private val context: Context,
    savedState: SavedState,
    private val counterUseCase: CounterUseCase
) : AbstractViewModel() {

    @Argument
    private val counterName: String by savedState.required()

    val counterText: LiveData<String> = counterUseCase.liveCounter.map { counter ->
        counter?.value?.toString() ?: context.getString(R.string.counter_no_value)
    }

    val loadingVisibility: LiveData<Int> = counterUseCase.isLoading.map { isLoading ->
        if (isLoading) View.VISIBLE else View.GONE
    }

    val isOperationEnabled: LiveData<Boolean> = counterUseCase.liveCounter.map { counter ->
        counter != null
    }

    fun loadData() {
        launch {
            counterUseCase.loadCounter(counterName)
        }
    }

    fun countUp() {
        launch {
            counterUseCase.changeCounterValue(1)
        }
    }

    fun countDown() {
        launch {
            counterUseCase.changeCounterValue(-1)
        }
    }

    fun deleteCounter() {
        launch {
            counterUseCase.deleteCounter()
        }
    }

    companion object : ViewModelFactory<SampleViewModel>() {
        override fun createViewModel(context: Context, savedState: SavedState): SampleViewModel =
            context.getComponent(ApplicationGraph).viewModelsGraphFactory().create(savedState)
                .sampleViewModel().also {
                    it.loadData()
                }
    }
}
