package com.explorer.fileexplorer.core.storage

import java.io.InputStream
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/**
 * Small Shizuku UserService used by the file repository. It intentionally exposes one
 * bounded command surface to the app process; all path validation and command construction
 * stays in [ShizukuFileRepository].
 */
class ShizukuShellService : IShizukuShellService.Stub() {
    override fun execute(command: String): String {
        require(command.length <= MAX_COMMAND_LENGTH) { "Shizuku command is too long" }
        val process = ProcessBuilder("sh", "-c", command)
            .redirectErrorStream(true)
            .start()
        val outputRef = AtomicReference(BoundedOutput("", false))
        val readerThread = Thread {
            runCatching {
                val result = readBounded(process.inputStream, MAX_OUTPUT_BYTES)
                outputRef.set(result)
                if (result.truncated) process.destroyForcibly()
            }
        }.apply {
            isDaemon = true
            start()
        }
        return try {
            val finished = process.waitFor(COMMAND_TIMEOUT_MS, TimeUnit.MILLISECONDS)
            if (!finished) {
                process.destroyForcibly()
                readerThread.join(READER_JOIN_TIMEOUT_MS)
                "$EXIT_CODE_TIMEOUT\nShizuku command timed out after ${COMMAND_TIMEOUT_MS / 1000}s"
            } else {
                readerThread.join(READER_JOIN_TIMEOUT_MS)
                val output = outputRef.get()
                "${process.exitValue()}\n${output.text}"
            }
        } finally {
            runCatching { process.inputStream.close() }
            if (process.isAlive) process.destroyForcibly()
        }
    }

    private data class BoundedOutput(
        val text: String,
        val truncated: Boolean,
    )

    private fun readBounded(input: InputStream, maxBytes: Int): BoundedOutput {
        val output = StringBuilder()
        val buffer = ByteArray(8192)
        var total = 0
        while (total < maxBytes) {
            val read = input.read(buffer, 0, minOf(buffer.size, maxBytes - total))
            if (read < 0) break
            if (read == 0) continue
            output.append(String(buffer, 0, read, Charsets.UTF_8))
            total += read
        }
        return BoundedOutput(output.toString(), total >= maxBytes)
    }

    companion object {
        private const val MAX_COMMAND_LENGTH = 16 * 1024
        private const val MAX_OUTPUT_BYTES = 4 * 1024 * 1024
        private const val COMMAND_TIMEOUT_MS = 12_000L
        private const val READER_JOIN_TIMEOUT_MS = 1_000L
        private const val EXIT_CODE_TIMEOUT = 124
    }
}
