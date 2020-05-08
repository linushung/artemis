package io.linus.artemis.authorisation

import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import io.linus.artemis.persistence.entity.Role
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.security.KeyPair
import java.util.*

const val VALID_PERIOD_SEC = 3600
const val JWT_CLAIMS_ISSUER = "artemis-MockIdentityManager"
const val JWT_CLAIMS_AUDIENCE = "angular-realworld"

data class JWTClaims(val sub: String, val jti: String, val username: String ,val role: String)

interface JWTService {
    fun fetchJWT(claims: JWTClaims) : String
    fun verifyJWT(jwt: String): UserDetails
}

/* https://github.com/jwtk/jjwt#quickstart */
@Service
class MockIdentityManager: JWTService {
    private val key: KeyPair = Keys.keyPairFor(SignatureAlgorithm.RS512)

    override fun fetchJWT(claims: JWTClaims): String {
        val currentDate = Date(System.currentTimeMillis())
        val expiredDate = Date(System.currentTimeMillis() + VALID_PERIOD_SEC * 1000)

        return Jwts.builder()
                .setIssuer(JWT_CLAIMS_ISSUER)
                .setSubject(claims.sub)
                .setAudience(JWT_CLAIMS_AUDIENCE)
                .setIssuedAt(currentDate)
                .setExpiration(expiredDate)
                .setId(claims.jti)
                .claim("username", claims.username)
                .claim("role", claims.role)
                .signWith(key.private)
                .compact()
    }

    override fun verifyJWT(jwt: String): UserDetails {
         val token = Jwts.parserBuilder()
                .requireIssuer(JWT_CLAIMS_ISSUER)
                .requireAudience(JWT_CLAIMS_AUDIENCE)
                .setSigningKey(key.public)
                .build()
                .parseClaimsJws(jwt.replace("Bearer ", ""))
        return User(token.body["username"].toString(), "", listOf(SimpleGrantedAuthority(token.body["role"].toString())))
    }

}

/* https://tools.ietf.org/html/rfc7519#section-4.1 */
/**
 * Reserved claims:
 * iss (issuer): Issuer of the JWT
 * sub (subject): Subject of the JWT (the user)
 * aud (audience): Recipient for which the JWT is intended
 * exp (expiration time): Time after which the JWT expires
 * nbf (not before time): Time before which the JWT must not be accepted for processing
 * iat (issued at time): Time at which the JWT was issued; can be used to determine age of the JWT
 * jti (JWT ID): Unique identifier; can be used to prevent the JWT from being replayed (allows a token to be used only once)
 * */
