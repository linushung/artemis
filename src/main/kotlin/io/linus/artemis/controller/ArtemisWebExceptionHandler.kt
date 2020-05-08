package io.linus.artemis.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.jsonwebtoken.JwtException
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Component
class ArtemisWebExceptionHandler(private val objectMapper: ObjectMapper): ErrorWebExceptionHandler {

    override fun handle(exchange: ServerWebExchange, ex: Throwable): Mono<Void> {
        when (ex) {
            is JwtException -> exchange.response.statusCode = HttpStatus.UNAUTHORIZED
            else -> exchange.response.statusCode = HttpStatus.INTERNAL_SERVER_ERROR
        }

        exchange.response.run {
            writeWith { bufferFactory().wrap(objectMapper.writeValueAsBytes(ex)).toMono() }
        }

        return Mono.empty()
    }
}

@RestControllerAdvice
class JwtExceptionHandler {

    @ExceptionHandler(value = [JwtException::class])
    fun jwtExceptionHandler(exchange: ServerWebExchange, ex: JwtException): ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.message)
    }
}