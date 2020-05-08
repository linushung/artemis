package io.linus.artemis.controller

import io.linus.artemis.persistence.entity.*
import io.linus.artemis.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import javax.validation.Valid

/* Ref: https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-webflux */
/* Ref: https://docs.spring.io/spring/docs/current/spring-framework-reference/web-reactive.html#spring-webflux */

/* Error Handling
*  Ref: https://www.baeldung.com/exception-handling-for-rest-with-spring
*  Ref: https://blog.softwaremill.com/spring-webflux-and-domain-exceptions-10ae2096b159
*  Ref: https://blog.softwaremill.com/spring-webflux-and-domain-validation-errors-3e0fc0f8c7a8
*/

@RestController
@RequestMapping(
        path = ["/api"],
        /* JSON is default opinion when Jackson on the classpath */
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_STREAM_JSON_VALUE]
)
class UserController(private val service: UserService) {

    /* Ref: https://www.baeldung.com/spring-response-entity */
    @PostMapping("/users")
    fun registerUser(@Valid @RequestBody register: RegisterReq): ResponseEntity<Mono<User>> {
        return ResponseEntity.status(HttpStatus.OK).body(service.create(register).toMono())
    }

    @PostMapping("/users/login")
    fun loginUser(@Valid @RequestBody login: LoginReq, response: ServerHttpResponse): ResponseEntity<Mono<String>> {
        return ResponseEntity.status(HttpStatus.OK).body(
                service.login(login).doOnError { throw ResponseStatusException(HttpStatus.BAD_REQUEST, it.message) }
        )
    }

    @PutMapping("/users")
//    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    fun updateUser(@Valid @RequestBody req: UpdateReq): ResponseEntity<Mono<User>> {
        return ResponseEntity.status(HttpStatus.OK).body(
                service.update(req).doOnError { throw ResponseStatusException(HttpStatus.BAD_REQUEST, it.message) }
        )
    }

    @GetMapping("/users")
//    @PreAuthorize("hasRole('USER')")
    fun getUser(): ResponseEntity<Mono<User>> {
        return ResponseEntity.status(HttpStatus.OK).body(
                service.currentUser().doOnError { throw ResponseStatusException(HttpStatus.BAD_REQUEST, it.message) }
        )
    }

    @GetMapping("/profiles/{username}")
    fun getProfile(@PathVariable username: String): ResponseEntity<Mono<Profile>> {
        return ResponseEntity.status(HttpStatus.OK).body(
                service.getProfile(username).doOnError { throw ResponseStatusException(HttpStatus.BAD_REQUEST, it.message) }
        )
    }

    @PostMapping("/profiles/{username}/follow")
    fun followUser(@PathVariable username: String): ResponseEntity<Mono<Profile>> {
        return ResponseEntity.status(HttpStatus.OK).body(
                service.followUser(username).doOnError { throw ResponseStatusException(HttpStatus.BAD_REQUEST, it.message) }
        )
    }

    @DeleteMapping("/profiles/{username}/follow")
    fun unFollowUser(@PathVariable username: String): ResponseEntity<Mono<Profile>> {
        return ResponseEntity.status(HttpStatus.OK).body(
                service.unFollowUser(username).doOnError { throw ResponseStatusException(HttpStatus.BAD_REQUEST, it.message) }
        )
    }

}