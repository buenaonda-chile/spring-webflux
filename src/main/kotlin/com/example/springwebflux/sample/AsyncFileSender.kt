package com.example.springwebflux.sample

interface AsyncFileSender {
    fun sendAllFiles(files: List<String>)
}