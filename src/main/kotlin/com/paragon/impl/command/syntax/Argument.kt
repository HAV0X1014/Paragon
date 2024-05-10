package com.paragon.impl.command.syntax

/**
 * @author Surge
 * @since 27/11/2022
 */
class Argument(
    val builder: SyntaxBuilder,
    val name: String,
    val valid: Array<String>,
    visibleWhen: Array<Pair<String, String>> = arrayOf()
) {

    private val visibilityFactors = ArrayList<Pair<Int, String>>()

    init {
        visibleWhen.forEach { pair ->
            visibilityFactors.add(Pair(builder.arguments.indexOf(builder.arguments.firstOrNull {
                it.name.equals(
                    pair.first,
                    true
                )
            }), pair.second))
        }
    }

    fun isComplete(input: String) = valid.any { it.equals(input, true) } || valid[0].equals("any_str", false)

    fun isVisible(args: ArrayList<String>): Boolean {
        if (visibilityFactors.isEmpty()) {
            return true
        }

        visibilityFactors.forEach {
            if (it.second.startsWith(args[it.first], true)) {
                return true
            }
        }

        return false
    }

    fun getFirstValidOption(input: String): String {
        if (!valid.any { it.startsWith(input, true) }) {
            return ""
        }

        return valid.first { it.startsWith(input, true) }.replace("any_str", name)
    }

}