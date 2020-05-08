package io.linus.artemis.security

import io.linus.artemis.persistence.repository.UserRepository
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

/* Rer: https://spring.io/guides/topicals/spring-security-architecture */

/* Rer: https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#boot-features-security-mvc */
//@EnableWebSecurity
class WebSecurityConfig(private val userDetailsSvc: UserDetailsService) : WebSecurityConfigurerAdapter() {

    override fun configure(auth: AuthenticationManagerBuilder) {
        auth.userDetailsService(userDetailsSvc)
    }

    override fun configure(http: HttpSecurity) {
        http.cors().and().csrf().disable()
                .authorizeRequests()
                .antMatchers("/api/users/login").permitAll()
                .anyRequest().authenticated().and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .formLogin().disable()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}

class WebUserDetailsService(private val userRepo: UserRepository): UserDetailsService {
    /**
     * Locates the user based on the username. In the actual implementation, the search
     * may possibly be case sensitive, or case insensitive depending on how the
     * implementation instance is configured. In this case, the `UserDetails`
     * object that comes back may have a username that is of a different case than what
     * was actually requested..
     *
     * @param username the username identifying the user whose data is required.
     *
     * @return a fully populated user record (never `null`)
     *
     * @throws UsernameNotFoundException if the user could not be found or the user has no
     * GrantedAuthority
     */
    override fun loadUserByUsername(email: String): UserDetails {
        return userRepo.findByEmail(email).orElseThrow {
            UsernameNotFoundException("Invalid email: $email")
        }.let {
            User(it.username, it.password, mutableListOf())
        }

//        return posterRepo.findByEmail(email).blockOptional().orElseThrow {
//            UsernameNotFoundException("Invalid email: $email")
//        }.let {
//            User(it.username, it.password, mutableListOf())
//        }

    }
}
