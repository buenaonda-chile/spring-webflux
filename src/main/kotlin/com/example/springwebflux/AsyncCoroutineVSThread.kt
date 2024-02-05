package com.example.springwebflux

import com.example.springwebflux.sample.AsyncFileSenderCoroutineImpl
import com.example.springwebflux.sample.AsyncFileSenderThreadImpl
import java.time.LocalDateTime

fun main() {
    var start = System.currentTimeMillis()
    val sender1 = AsyncFileSenderCoroutineImpl()
    val files = (1..1000).map { it.toString() }
    sender1.sendAllFiles(files)
    var end = System.currentTimeMillis()
    var executedTimeSeconds = (end - start) / 1000.0
    log("Coroutine Sender executed time: $executedTimeSeconds seconds")

    start = System.currentTimeMillis()
    val sender2 = AsyncFileSenderThreadImpl()
    sender2.sendAllFiles(files)
    end = System.currentTimeMillis()
    executedTimeSeconds = (end - start) / 1000.0
    log("Thread Sender executed time: $executedTimeSeconds seconds")
}

private fun now(): LocalDateTime = LocalDateTime.now()
private fun log(message: String) = println("${now()}:${Thread.currentThread()}:$message")