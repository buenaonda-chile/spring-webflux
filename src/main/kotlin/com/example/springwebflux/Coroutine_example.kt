package com.example.springwebflux

import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis


//Dispatcher 경량 쓰레드 관리 범위
//Default : 기본적으로 제공되는 dispatcher CPU 코어수에 따라 결정
//IO : I/O Operation에 해당되는 공통된 pool 사용
//Unconfined : 코루틴 시작한 쓰레드에서 코루틴 시작 -> 중간처리는 다른 쓰레드에서도 완성될수도있음

suspend fun createCoroutines(amount: Int) {
    val jobs = arrayListOf<Job>()
    for (i in 1..amount)
        jobs += CoroutineScope(Dispatchers.Default).launch {
            println("Start $i in ${Thread.currentThread().name}")
            delay(1000)
            println("End $i in ${Thread.currentThread().name}")
        }
    jobs.forEach { it.join() }
}

fun main() = runBlocking {
    println("${Thread.activeCount()} thread active counts start")
    val time = measureTimeMillis {
        createCoroutines(10_000)
    }
    println("${Thread.activeCount()} thread active counts end")
    println("Take $time ms")
}