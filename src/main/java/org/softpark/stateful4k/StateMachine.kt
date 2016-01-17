package org.softpark.stateful4k

import org.softpark.stateful4k.action.Executor
import org.softpark.stateful4k.action.IExecutor
import org.softpark.stateful4k.config.Configurator
import org.softpark.stateful4k.config.IConfigurationProvider
import org.softpark.stateful4k.config.IConfigurator

object StateMachine {
    @JvmStatic
    fun <C, S : Any, E : Any> createConfigurator(): IConfigurator<C, S, E> =
            Configurator()

    @JvmStatic
    fun <C, S : Any, E : Any> createExecutor(
            configuration: IConfigurationProvider<C, S, E>,
            context: C, initialState: S)
            : IExecutor<C, S, E> =
            Executor(configuration, context, initialState)
}
