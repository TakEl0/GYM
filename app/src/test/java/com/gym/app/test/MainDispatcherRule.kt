/**
 * @file MainDispatcherRule.kt
 * @brief Regla de JUnit que sustituye el dispatcher principal por un TestDispatcher.
 * Permite probar ViewModels que lanzan corrutinas en Dispatchers.Main.
 */
package com.gym.app.test

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * @class MainDispatcherRule
 * @brief Implementa un TestWatcher que instala un [TestDispatcher] como Main
 * antes de cada test y lo restaura al finalizar.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}