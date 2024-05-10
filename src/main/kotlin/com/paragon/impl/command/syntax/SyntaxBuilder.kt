package com.paragon.impl.command.syntax

/**
 * @author Surge
 * @since 27/11/2022
 */
open class SyntaxBuilder {

    val arguments = arrayListOf<Argument>()

    /**
     * Adds an argument to the syntax.
     */
    fun addArgument(argument: Argument) = arguments.add(argument).let { return@let this }

    fun join() = arguments.joinToString {
        "[${it.name}: <${it.valid.joinToString("|", transform = { valid -> valid })}>]"
    }

    companion object {

        @JvmStatic
        fun createBuilder(arguments: ArrayList<ArgumentData>): SyntaxBuilder {
            val builder = SyntaxBuilder()

            arguments.forEach {
                builder.addArgument(Argument(builder, it.name, it.valid, it.visibleWhen))
            }

            return builder
        }

    }


}