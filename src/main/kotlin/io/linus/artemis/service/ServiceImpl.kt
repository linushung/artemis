package io.linus.artemis.service

import io.linus.artemis.authorisation.JWTClaims
import io.linus.artemis.authorisation.JWTService
import io.linus.artemis.persistence.entity.*
import io.linus.artemis.persistence.repository.FollowerRepository
import io.linus.artemis.persistence.repository.UserRepository
import io.linus.artemis.security.ArtemisUserDetailsService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import kotlin.coroutines.CoroutineContext

@Service
class UserServiceImpl(
        val jwtService: JWTService,
        val encoder: PasswordEncoder,
        val userRepo: UserRepository,
        val followerRepo: FollowerRepository,
        val userDetailsSvc: ArtemisUserDetailsService,
        @Qualifier("UserDetailsRepositoryReactiveAuthenticationManage")
        val authMgr: ReactiveAuthenticationManager
) : UserService, CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Job() + Dispatchers.IO

    @Transactional
    override fun create(register: RegisterReq): User {
        val p = User(register.email, register.username, encoder.encode(register.password), Role.USER.role)
        userRepo.save(p)
        return p
    }

    override fun login(login: LoginReq): Mono<String> {
        return authMgr.authenticate(UsernamePasswordAuthenticationToken(login.email, login.password))
                .flatMap {
                    userDetailsSvc.findByUsername(login.email)
                            .map {
                                val token = JWTClaims(login.email, login.email, it.username, it.authorities.map { auth -> auth.authority }[0])
                                jwtService.fetchJWT(token)
                            }
                }
    }

    @Transactional
    override fun update(info: UpdateReq): Mono<User> {
        return ReactiveSecurityContextHolder.getContext()
                .map { userRepo.findByUsername(it.authentication.principal as String) }
                .flatMap {
                    when (it.isPresent) {
                        true -> {
                            it.get().run {
                                if (info.email.isNotBlank()) {
                                    email = info.email
                                }
                                if (info.username.isNotBlank()) {
                                    username = info.username
                                }
                                if (info.password.isNotBlank()) {
                                    password = info.password
                                }
                                if (info.image.isNotBlank()) {
                                    image = info.image
                                }
                                if (info.bio.isNotBlank()) {
                                    bio = info.bio
                                }
                                toMono()
                            }
                        }
                        false -> Mono.error(UsernameNotFoundException("Cannot update the info of current user..."))
                    }
                }
    }

    override fun currentUser(): Mono<User> {
        return ReactiveSecurityContextHolder.getContext()
                .map { userRepo.findByUsername(it.authentication.principal as String) }
                .flatMap {
                    when (it.isPresent) {
                        true -> Mono.just(it.get())
                        false -> Mono.error(UsernameNotFoundException("Cannot find the info of current user..."))
                    }
                }
    }

    override fun getProfile(username: String): Mono<Profile> {
        return ReactiveSecurityContextHolder.getContext()
                .map { ctx ->
                    userRepo.findByUsername(username)
                            .map { user ->
                                val result= followerRepo.findByEmailAndFollower(user.email, ctx.authentication.principal as String)
                                Profile(username, user.bio, user.image, result.isPresent)
                            }
                }
                .map { it.get() }
    }

    override fun followUser(username: String): Mono<Profile> {
        return ReactiveSecurityContextHolder.getContext()
                .map { ctx ->
                    userRepo.findByUsername(username)
                            .map {
                                followerRepo.save(Follower(it.email, ctx.authentication.principal as String, it))
                                Profile(username, it.bio, it.image, true)
                            }
                }
                .map { it.get() }
    }

    override fun unFollowUser(username: String): Mono<Profile> {
        return ReactiveSecurityContextHolder.getContext()
                .map { ctx ->
                    userRepo.findByUsername(username)
                            .map {
                                followerRepo.delete(Follower(it.email, ctx.authentication.principal as String, it))
                                Profile(username, it.bio, it.image, false)
                            }
                }
                .map { it.get() }
    }
}
