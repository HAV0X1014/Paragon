package com.paragon.bus.listener

import java.lang.invoke.LambdaMetafactory
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer

/**
 * An object which holds the data of a subscribed method
 *
 * @author Surge, therealbush
 * @since 05/03/22
 */
class SubscribedMethod(val source: Any?, method: Method) {

    // Wrapper for invoking the method
    private var handler: Consumer<Any>? = null

    init {
        if (handlerCache.containsKey(method)) {
            handler = handlerCache[method]
        } else {
            try {
                // Get lookup instance
                val lookup = MethodHandles.lookup()

                // Check method modifiers for static
                val isStatic = Modifier.isStatic(method.modifiers)

                // Create methodType for invoking the methodHandle
                val targetSignature = MethodType.methodType(Consumer::class.java)

                // Generate callsite
                val callSite = LambdaMetafactory.metafactory(
                    lookup,  // The lookup instance to use
                    "accept",  // The name of the method to implement
                    if (isStatic) targetSignature else targetSignature.appendParameterTypes(source?.javaClass),  // The signature for .invoke()
                    MethodType.methodType(Void.TYPE, Any::class.java),  // The method signature to implement
                    lookup.unreflect(method),  // Method to invoke when called
                    MethodType.methodType(Void.TYPE, method.parameterTypes[0]) // Signature that is enforced at runtime
                )

                // Get target to invoke
                val target = callSite.target

                // Invoke on the object if not static
                handler = (if (isStatic) target.invoke() else target.invoke(source)) as Consumer<Any>

                // Cache this dynamic handler
                handlerCache[method] = handler
            } catch (throwable: Throwable) {
                // This shouldn't ever happen
                throw Error(throwable)
            }
        }
    }

    operator fun invoke(event: Any) {
        handler!!.accept(event)
    }

    companion object {
        // Avoids creation of duplicates
        private val handlerCache: MutableMap<Method, Consumer<Any>?> = ConcurrentHashMap()
    }

}