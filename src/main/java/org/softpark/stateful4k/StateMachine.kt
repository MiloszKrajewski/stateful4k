package org.softpark.stateful4k

import org.softpark.stateful4k.action.*
import org.softpark.stateful4k.config.*

object StateMachine {
    @JvmStatic
    fun <C, S: Any, E: Any> createConfigurator(): IConfigurator<C, S, E> {
        return Configurator()
    }

    @JvmStatic
    fun <C, S: Any, E: Any> createExecutor(
            configuration: IConfigurationProvider<C, S, E>, context: C, initialState: S)
            : IExecutor<C, S, E> {
        return Executor(configuration, context, initialState)
    }
}
