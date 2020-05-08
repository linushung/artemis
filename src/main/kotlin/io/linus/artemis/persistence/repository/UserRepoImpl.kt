package io.linus.artemis.persistence.repository

import io.linus.artemis.persistence.entity.Follower
import io.linus.artemis.persistence.entity.User
import org.springframework.data.repository.CrudRepository
import java.util.*

/* Spring Data JPA implementation */
interface UserRepository: CrudRepository<User, Long> {
    fun findByEmail(email: String): Optional<User>
    fun findByUsername(username: String): Optional<User>
    fun findByEmailAndPassword(email: String, password: String): Optional<User>
}

interface FollowerRepository: CrudRepository<Follower, Long> {
    /* @IdClass */
    fun findByEmail(email: String): Optional<List<Follower>>
    fun findByEmailAndFollower(email: String, follower: String): Optional<Follower>
    /* @EmbeddedId */
//    fun findByFollowerIdEmail(email: String): Optional<Follower>
}

/**
 * Both Spring Data JPA & Hibernate Panache use `Repository` word, but they actually implement DAO layer in essence.
 *
 * Data Access Objects are mostly useful when you have one or more of the following situations:
 * - The entity type is shared between projects written for different stacks. One project will use DAOs written for
 *   WildFly, another for Spring.
 * - The entity type is shared between projects written for different use-cases. One project will handle the entity in
 *   one way, while another will differ entirely.
 * - You need to mock your DAOs in tests.
 * - Your entity type is crammed so full of getters and setters that adding any model method will exceed the maximum
 *   method count.
 *
 * If you don’t absolutely need DAOs, they come with drawbacks:
 * - You need to have one extra class per entity.
 * - You need to inject DAOs everywhere you use them.
 * - You cannot inject DAOs in methods without going out and adding a field, making this quite costly in terms of
 *   editing flow.
 * - You cannot discover a DAO methods without injecting it and trying completion. If this is not the DAO you’re looking
 *   for, you need to go back to the injected field and change its type and name to try again.
 * - Your IDEs are not helping you with any of these drawbacks.
 * - Any model refactoring requires you to examine queries in the DAO that corresponds to the entity you modified,
 *   making this poorly encapsulated.
 * */