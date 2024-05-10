package com.paragon.util.string

/**
 * @author Surge
 * @author SooStrator1136
 */
object StringUtil {

    /**
     * Formats an enum name (in SCREAMING_SNAKE_CASE) to UpperCamelCase.
     *
     * @return the converted name.
     */
    @JvmStatic
    fun getFormattedText(enumIn: Enum<*>) = enumIn.name.let { text ->
        buildString(text.length) {
            var isFirst = true
            text.forEach {
                if (it == '_') {
                    isFirst = true
                    return@forEach
                }

                if (isFirst) {
                    append(it.toString().uppercase())
                    isFirst = false
                } else {
                    append(it.toString().lowercase())
                }
            }
        }
    }

    @JvmStatic
    fun wrap(str: String, length: Int) = buildString {
        var lastDeliminatorPosition = 0

        str.split(" ").forEach {
            if (this.length - lastDeliminatorPosition + it.length > length) {
                append(System.lineSeparator()).append(it)
                lastDeliminatorPosition = this.length + 1
            } else {
                append(if (this.isEmpty()) "" else " ").append(it)
            }
        }
    }

}