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
package com.linecorp.lich.dagger_sample.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.linecorp.lich.dagger_sample.db.CounterDao
import com.linecorp.lich.dagger_sample.entity.Counter
import com.linecorp.lich.dagger_sample.remote.CounterServiceClient
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class CounterRepositoryTest {

    private lateinit var mockCounterServiceClient: CounterServiceClient

    private lateinit var mockCounterDao: CounterDao

    private lateinit var counterRepository: CounterRepository

    @Before
    fun setUp() {
        mockCounterServiceClient = mockk()
        mockCounterDao = mockk(relaxUnitFun = true)
        counterRepository = CounterRepository(mockCounterServiceClient, mockCounterDao)
    }

    @Test
    fun getCounterExisting() = runBlocking {
        val counterName = "foo"
        val counter = Counter(counterName, 20)
        coEvery { mockCounterDao.find(counterName) } returns counter

        val actual = counterRepository.getCounter(counterName)

        assertEquals(CounterResult.Success(counter), actual)
        coVerify(exactly = 0) { mockCounterServiceClient.getInitialCounterValue(any()) }
    }

    @Test
    fun getCounterNew() = runBlocking {
        val counterName = "foo"
        coEvery { mockCounterDao.find(any()) } returns null
        coEvery { mockCounterServiceClient.getInitialCounterValue(counterName) } returns 42

        val actual = counterRepository.getCounter(counterName)

        val counter = Counter(counterName, 42)
        assertEquals(CounterResult.Success(counter), actual)
        coVerify(exactly = 1) { mockCounterDao.replace(counter) }
    }

    @Test
    fun getCounterNetworkError() = runBlocking {
        coEvery { mockCounterDao.find(any()) } returns null
        coEvery { mockCounterServiceClient.getInitialCounterValue(any()) } throws IOException()

        val actual = counterRepository.getCounter("foo")

        assertEquals(CounterResult.NetworkError, actual)
    }
}
