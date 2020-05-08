package io.linus.artemis.security

import io.linus.artemis.authorisation.JWTService
import io.linus.artemis.persistence.repository.UserRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.DelegatingReactiveAuthenticationManager
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.server.resource.web.server.ServerBearerTokenAuthenticationConverter
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

/* Ref: https://docs.spring.io/spring-security/site/docs/current/reference/html5/ */
/* Ref: https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#boot-features-security-webflux */

/* Issue: https://stackoverflow.com/questions/51598282/custom-authentication-with-spring-security-and-reactive-spring */
/* Issue: https://stackoverflow.com/questions/56056404/disable-websession-creation-when-using-spring-security-with-spring-webflux */
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class ArtemisSecurityConfig {

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity, jwtAuthMgr: ArtemisJwtAuthenticationManager): SecurityWebFilterChain {
        return http
        /* Ref: https://developer.mozilla.org/en-US/docs/Web/HTTP/Authentication */
                .httpBasic().disable()
                .formLogin().disable()
                .csrf().disable()
                .logout().disable()
                .authorizeExchange()
                .pathMatchers("/actuator/*").permitAll()
                .pathMatchers(HttpMethod.POST,"/api/users").permitAll()
                .pathMatchers(HttpMethod.POST,"/api/users/login").permitAll()
                .anyExchange().authenticated()
                .and()
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .addFilterAt(authenticationWebFilter(jwtAuthMgr), SecurityWebFiltersOrder.AUTHENTICATION)
                .build()
    }

    @Bean
    fun authenticationWebFilter(jwtAuthMgr: ArtemisJwtAuthenticationManager): AuthenticationWebFilter =
            AuthenticationWebFilter(jwtAuthMgr).apply {
                setServerAuthenticationConverter(ServerBearerTokenAuthenticationConverter())
            }

    @Bean(name = ["UserDetailsRepositoryReactiveAuthenticationManager"])
    fun basicAuthenticationManager(userDetailService: ReactiveUserDetailsService): ReactiveAuthenticationManager =
            UserDetailsRepositoryReactiveAuthenticationManager(userDetailService)

    /**
     * The DelegatingPasswordEncoder automatically uses the latest and greatest encryption algorithm and therefore
     * is the best choice to be used as PasswordEncoder.
     * It's default of AbstractUserDetailsReactiveAuthenticationManager
     */
    @Bean
    fun passwordEncoder(): PasswordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()

    fun artemisDelegatingReactiveAuthenticationManager(
            basicAuthenticationManager: UserDetailsRepositoryReactiveAuthenticationManager,
            jwtAuthenticationManager: ArtemisJwtAuthenticationManager
    ): DelegatingReactiveAuthenticationManager =
            DelegatingReactiveAuthenticationManager(listOf(basicAuthenticationManager, jwtAuthenticationManager))
}

@Service
class ArtemisUserDetailsService(val userRepo: UserRepository) : ReactiveUserDetailsService {

    override fun findByUsername(email: String): Mono<UserDetails> {
        return userRepo.findByEmail(email).run {
            when (isEmpty) {
                true -> Mono.error(UsernameNotFoundException("Authentication fail..."))
                false -> Mono.just(User(get().username, get().password, listOf(SimpleGrantedAuthority(get().role))))
            }
        }
    }
}

@Primary
@Component
class ArtemisJwtAuthenticationManager(private val jWTService: JWTService): ReactiveAuthenticationManager {

    override fun authenticate(authentication: Authentication): Mono<Authentication> {
        return jWTService.verifyJWT(authentication.credentials as String).toMono().map {
            UsernamePasswordAuthenticationToken(it.username, it.password, it.authorities)
        }
    }
}
