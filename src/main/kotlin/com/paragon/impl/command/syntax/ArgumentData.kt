package com.paragon.impl.command.syntax

/**
 * @author Surge
 * @since 28/11/2022
 */
data class ArgumentData(
    val name: String,
    val valid: Array<String>,
    val visibleWhen: Array<Pair<String, String>> = arrayOf()
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (javaClass != other?.javaClass) {
            return false
        }

        other as ArgumentData

        if (name != other.name) {
            return false
        }
        if (!valid.contentEquals(other.valid)) {
            return false
        }
        if (!visibleWhen.contentEquals(other.visibleWhen)) {
            return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + valid.contentHashCode()
        result = 31 * result + visibleWhen.contentHashCode()
        return result
    }

}