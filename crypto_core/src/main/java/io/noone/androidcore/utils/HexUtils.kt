package io.noone.androidcore.utils

object HexUtils {

    fun toBytes(input: String): ByteArray {
        if (input.length % 2 != 0)
            throw IllegalArgumentException("Input string must contain an even number of characters")

        val charArray = input.toCharArray()
        val byteArraySize = charArray.size / 2
        val output = ByteArray(byteArraySize)
        var index = 0
        while (index < byteArraySize) {
            val byteIndex = index * 2
            val firstChar = charArray[byteIndex].digitToIntOrNull(16) ?: -1
            val secondChar = charArray[byteIndex + 1].digitToIntOrNull(16) ?: -1

            if (firstChar >= 0 && secondChar >= 0) {
                var byte = firstChar shl 4 or secondChar
                if (byte > 127) { byte -= 256 }
                output[index] = byte.toByte()
                index++
            } else {
                throw IllegalArgumentException("Invalid hex digit " + charArray[byteIndex] + charArray[byteIndex + 1])
            }
        }
        return output
    }

    fun toHex(
        input: ByteArray,
        offset: Int,
        limit: Int,
        separator: String?
    ): String {
        val outputBuilder = StringBuilder()
        var index = 0
        while (index < limit) {
            val j = input[index + offset].toInt() and 0xFF
            if (j < 16) {
                outputBuilder.append("0")
            }
            outputBuilder.append(Integer.toHexString(j))
            if (separator != null && index + 1 < limit) {
                outputBuilder.append(separator)
            }
            index += 1
        }
        return outputBuilder.toString()
    }

    @JvmOverloads
    fun toHex(input: ByteArray, separator: String? = null): String {
        return toHex(input, 0, input.size, separator)
    }
}