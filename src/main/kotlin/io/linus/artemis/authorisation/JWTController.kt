package io.linus.artemis.authorisation

import io.linus.artemis.persistence.entity.Role
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping(path = ["/artemis"])
class JWTController(val jwtService: JWTService) {

    @GetMapping("/jwt")
    fun getJWT(): ResponseEntity<String> {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(jwtService.fetchJWT(JWTClaims("artemis","linushung@gmail.com", "Linus", Role.ADMIN.name)))
    }
}
