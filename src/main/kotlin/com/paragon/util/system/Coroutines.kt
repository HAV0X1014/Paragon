package com.paragon.util.system

import com.google.common.util.concurrent.ListenableFuture
import com.paragon.util.mc
import kotlinx.coroutines.*
import net.minecraft.crash.CrashReport
import kotlin.coroutines.CoroutineContext
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

// FROM 711.CLUB vvv

private val defaultContext = Dispatchers.Default + CoroutineExceptionHandler { context, throwable ->
    mc.crashed(
        CrashReport(
            """
            Paragon: An uncaught exception was thrown from a coroutine. This means something 
            bad happened that would probably make the game unplayable if it wasn't shut down.
            
            Context: $context
            
            DM the devs and tell them to fix their shitcode! (also please send them this whole log)
            
            """.trimIndent(), throwable
        )
    )
}

object Background : CoroutineScope by CoroutineScope(defaultContext), CoroutineContext by defaultContext

// big
fun mainThread(block: () -> Unit): ListenableFuture<Any> = mc.addScheduledTask(block)

// BIG
fun backgroundThread(block: suspend CoroutineScope.() -> Unit) = Background.launch(block = block)

fun CoroutineScope.lazyLaunch(block: suspend CoroutineScope.() -> Unit) = launch(
    start = CoroutineStart.LAZY, block = block
)

/**
 * Runs the given block on another thread. If the delegated
 * property is called before the thread finishes, the
 * calling thread will be paused until it finishes.
 *
 * @author bush
 * @since 2/17/2022
 */
class AsyncDelegate<T>(block: suspend CoroutineScope.() -> T) : ReadWriteProperty<Any?, T> {
    private val deferred = Background.async { block() }
    private var value: T? = null

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        // I know this will go every time if the .await()'ed value is null, but I cba to change this
        if (value == null) value = runBlocking { deferred.await() }
        return value!!
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }
}

fun <T> async(block: suspend CoroutineScope.() -> T) = AsyncDelegate(block)