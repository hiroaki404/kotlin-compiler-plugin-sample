package org.example.app

import com.example.log.Log

@Log
class MyService    // "MyService initialized" が自動出力される

fun main() {
    MyService()
}
