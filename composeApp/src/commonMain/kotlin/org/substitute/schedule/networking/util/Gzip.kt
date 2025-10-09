package org.substitute.schedule.networking.util

import okio.Buffer
import okio.GzipSink
import okio.GzipSource
import okio.buffer

object Gzip {
    fun compress(data: String): ByteArray {
        val buffer = Buffer()
        val gzipSink = GzipSink(buffer).buffer()

        gzipSink.writeUtf8(data)
        gzipSink.close()

        return buffer.readByteArray()
    }

    fun decompress(data: ByteArray): String {
        val buffer = Buffer().write(data)
        val gzipSource = GzipSource(buffer).buffer()

        return gzipSource.readUtf8()

    }
}