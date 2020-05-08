package io.linus.artemis.service

import io.linus.artemis.persistence.entity.*
import reactor.core.publisher.Mono

interface UserService {

    fun create(register: RegisterReq): User

    fun login(login: LoginReq): Mono<String>

    fun update(info: UpdateReq): Mono<User>

    fun currentUser(): Mono<User>

    fun getProfile(username: String): Mono<Profile>

    fun followUser(username: String): Mono<Profile>

    fun unFollowUser(username: String): Mono<Profile>
}
