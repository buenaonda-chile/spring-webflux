package com.example.springwebflux.sample

import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class AsyncFileSenderThreadImpl (
    private val chunkSize: Int = 100,
    private val maxRetries: Int = 3
): AsyncFileSender {
    private val successFiles = Collections.synchronizedList(mutableListOf<String>())
    private val failedFiles = Collections.synchronizedList(mutableListOf<String>())

    // 스레드 풀을 생성하여 각각 job을 제출해준다.
    override fun sendAllFiles(files: List<String>) {
        val executor = Executors.newFixedThreadPool(200)
        files.chunked(chunkSize).forEachIndexed { chunkIndex, chunk ->
            chunk.forEachIndexed { fileIndex, file ->
                executor.submit { attemptToSendFile(file, chunkIndex, fileIndex) }
            }
        }
        executor.shutdown()
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)
        } catch (e: InterruptedException) {
            log("File sending interrupted: ${e.message}")
        }
        reportAndCleanUp()
    }

    private fun sendFile(file: String): String {
        // 코루틴과 다르게 50ms 대기를 Thread.sleep()으로 해준다.
        Thread.sleep(50)

        if (Random.nextDouble() < 0.1) {
            throw RuntimeException("Error while sending file: $file")
        }

        return file
    }

    private fun attemptToSendFile(file: String, chunkIndex: Int, fileIndex: Int) {
        repeat(maxRetries) {attempt ->
            try {
                val sentFile = sendFile(file)
                successFiles.add(sentFile)
                return
            } catch (e: RuntimeException) {
                if (attempt == maxRetries - 1) {
                    logFailureToSend(file, chunkIndex, fileIndex, attempt + 1, e.message)
                    failedFiles.add(file)
                }
            }
        }
    }

    private fun logFailureToSend(file: String, chunkIndex: Int, fileIndex: Int, attempts: Int, errorMessage: String?) {
        log("Failed to send file $file at chunk $chunkIndex, position $fileIndex after $attempts attempts: $errorMessage")
    }

    private fun reportAndCleanUp() {
        log("Number of successfully sent files: ${successFiles.size}")
        log("Number of failed files: ${failedFiles.size}")
        failedFiles.forEach { log("Failed file: $it") }

        successFiles.clear()
        failedFiles.clear()
    }

    fun now(): LocalDateTime = LocalDateTime.now()
    fun log(message: String) = println("${now()}:${Thread.currentThread()}:$message")
}