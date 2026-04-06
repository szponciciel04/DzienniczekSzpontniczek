package io.github.szpontium.api.prometheus

import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.SHA256

object POWCaptchaResolver {
    fun computeCaptchaResponse(
        challenge: String,
        difficulty: Long,
        rounds: Int,
    ): String {
        require(rounds >= 0) { "rounds must be non-negative" }
        require(difficulty in 0..0xFFFF_FFFFL)

        val provider = CryptographyProvider.Default
        val sha256 = provider.get(SHA256).hasher()

        val buf = ByteArray(512)
        var len = 0

        for (ch in challenge) buf[len++] = ch.code.toByte()

        fun writeAsciiLong(value: Long, start: Int): Int {
            var v = value
            var pos = start

            do {
                val digit = (v % 10).toInt()
                buf[pos++] = (digit + 48).toByte()
                v /= 10
            } while (v != 0L)

            var i = start
            var j = pos - 1
            while (i < j) {
                val tmp = buf[i]
                buf[i] = buf[j]
                buf[j] = tmp
                i++; j--
            }

            return pos
        }

        fun findNonce(baseLen: Int): Pair<Long, Int> {
            val upper = 1_000_000_000L

            var nonce = 1L
            var writeEnd: Int

            while (nonce <= upper) {
                writeEnd = writeAsciiLong(nonce, baseLen)

                val digest = sha256.hashBlocking(buf.copyOfRange(0, writeEnd))

                val value =
                    ((digest[0].toLong() and 0xFF) shl 24) or
                            ((digest[1].toLong() and 0xFF) shl 16) or
                            ((digest[2].toLong() and 0xFF) shl 8)  or
                            (digest[3].toLong() and 0xFF)

                if (value < difficulty) return nonce to writeEnd

                nonce++
            }

            throw IllegalStateException("Failed to find nonce within 1e9 attempts")
        }

        val results = LongArray(rounds)

        repeat(rounds) { index ->
            val (nonce, newLen) = findNonce(len)
            results[index] = nonce
            len = newLen
        }

        return results.joinToString(";")
    }
}
