package com.paragon.bus

import com.paragon.bus.listener.Listener
import com.paragon.bus.listener.SubscribedMethod
import java.lang.reflect.Method

/**
 * The main EventBus object. All events are posted from here, as well as objects being subscribed
 *
 * @author Surge
 * @since 05/03/22
 */
class EventBus {

    // A map of all classes and their subscribed methods
    private val subscribedMethods: MutableMap<Class<*>, ArrayList<SubscribedMethod>> = HashMap()

    /**
     * Register an object
     *
     * @param target The object to register
     */
    fun register(target: Any) {
        // Iterate through every method in the object's class
        target.javaClass.declaredMethods.forEach {
            // Check if the method is 'good'
            if (isMethodGood(it)) {
                // Register the method to the map
                registerMethod(it, target)
            }
        }
    }

    /**
     * Unregister an object
     *
     * @param obj The object to unregister
     */
    fun unregister(obj: Any) {
        // Iterate through every method in the map
        for (subscribedMethodList in subscribedMethods.values) {
            // Remove the method if the source is the obj parameter
            subscribedMethodList.removeIf { it.source === obj }
        }
    }

    /**
     * Registers an undefined amount of objects
     *
     * @param objList The list of objects to register
     */
    fun registerAll(vararg objList: Any) {
        for (obj in objList) {
            register(obj)
        }
    }

    /**
     * Unregisters an undefined amount of objects
     *
     * @param objList The list of objects to unregister
     */
    fun unregisterAll(vararg objList: Any) {
        for (obj in objList) {
            unregister(obj)
        }
    }

    /**
     * Registers a singular method
     *
     * @param method The method to register
     * @param obj The source
     */
    private fun registerMethod(method: Method, obj: Any?) {
        // Parameter type (event)
        val clazz = method.parameterTypes[0]

        // New subscribed method
        val subscribedMethod = SubscribedMethod(obj, method)

        // Set the method to accessible if it isn't already (private methods)
        if (!method.isAccessible) {
            method.isAccessible = true
        }

        // If the map contains the class, add the method to the class key
        if (subscribedMethods.containsKey(clazz)) {
            if (!subscribedMethods[clazz]!!.contains(subscribedMethod)) {
                subscribedMethods[clazz]!!.add(subscribedMethod)
            }
        }
        else {
            // Create new arraylist
            val array = ArrayList<SubscribedMethod>()

            // Add subscribed method to arraylist
            array.add(subscribedMethod)

            // Put the arraylist in subscribedMethods
            subscribedMethods[clazz] = array
        }
    }

    /**
     * Posts an object to trigger an event
     *
     * @param obj The object to post
     */
    fun post(obj: Any) {
        // Get the class
        val subscribedMethodList = subscribedMethods[obj.javaClass]

        // Check that we successfully got the class
        if (subscribedMethodList != null) {
            // Iterate through the subscribed methods
            for (subscribedMethod1 in subscribedMethodList) {
                subscribedMethod1.invoke(obj)
            }
        }
    }

    /**
     * Check if a method is good
     *
     * @param method The method to check
     * @return Whether it has one parameter (event), and it has the [Listener] annotation
     */
    private fun isMethodGood(method: Method): Boolean {
        return method.parameters.size == 1 && method.isAnnotationPresent(Listener::class.java)
    }

    /**
     * Check if an object is registered to the bus
     *
     * @param obj The object to check
     * @return Whether the object is registered
     */
    fun isRegistered(obj: Any) = subscribedMethods.containsKey(obj.javaClass)

}