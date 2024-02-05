package com.example.springwebflux.sample

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime
import kotlin.random.Random

class AsyncFileSenderCoroutineImpl(
    private val chunkSize: Int = 100,
    private val maxRetries: Int = 3
) : AsyncFileSender {
    private val successFiles = mutableListOf<String>()
    private val failedFiles = mutableListOf<String>()

    override fun sendAllFiles(files: List<String>) = runBlocking {
        files.chunked(chunkSize).mapIndexed { chunkIndex, chunk ->
            async {
                chunk.forEachIndexed { fileIndex, file ->
                    attemptToSendFile(file, chunkIndex, fileIndex)
                }
            }
        }.awaitAll()
        reportAndCleanUp()
    }

    // 실패를 하면 최대 3번까지 리트라이를 한다.
    // 실패한 파일들은 별도의 리스트에 저장한다.
    private suspend fun attemptToSendFile(file: String, chunkIndex: Int, fileIndex: Int) {
        repeat(maxRetries) { attempt ->
            try {
                val sentFile = sendFile(file)
                successFiles.add(sentFile)
                return
            } catch (e: RuntimeException) {
                if ( attempt == maxRetries - 1) {
                    logFailureToSend(file, chunkIndex, fileIndex, attempt + 1, e.message)
                    failedFiles.add(file)
                }
            }
        }
    }

    // 파일 전송은 50ms 소요되며, 10%확률로 실패하여 예외를 던진다.
    private suspend fun sendFile(file: String): String {
        delay(50)
        if (Random.nextDouble() < 0.1) {
            throw RuntimeException("Error while sending file: $file")
        }
        return file
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