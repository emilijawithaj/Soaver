package com.example.soavertriggertracker.vm

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher() //Dispatcher that is controllable (for testing). unconfined = deafult implementation, immediately runs them all as opposed to queueing them
) : TestWatcher() { //runs stuff before and after each test automatically (saves doing @BEfore and @After)
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher) //switched Main for test dispatcher
    }
    override fun finished(description: Description) {
        Dispatchers.resetMain() //resets to avoid leaks
    }
}