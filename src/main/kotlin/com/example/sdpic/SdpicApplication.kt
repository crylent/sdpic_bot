package com.example.sdpic

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SdpicApplication(sd: SD)

fun main(args: Array<String>) {
	runApplication<SdpicApplication>(*args)
}
