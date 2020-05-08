package io.linus.artemis.persistence.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonRootName
import java.io.Serializable
import java.util.*
import javax.persistence.*
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

const val EMAIL_REGEX = "(?:[a-z0-9!#\$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#\$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9]))\\.){3}(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9])|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])"

/* Ref: https://kotlinlang.org/docs/reference/compiler-plugins.html#jpa-support */

@Entity
@Table(name = "poster", indexes = [ Index(name = ",", columnList = "username") ])
@JsonRootName("user")
/* Ref: https://www.baeldung.com/jpa-entities */
/* Ref: https://www.baeldung.com/jackson-annotations */
/* Ref: https://www.baeldung.com/jackson-advanced-annotations */
data class User(
        @Id
        var email: String,
        var username: String,
        @JsonIgnore
        var password: String,
        var role: String,
        var image: String = "",
        var bio: String = "",
        @Column(length = 500)
        var token: String = ""
)

/* Ref: https://www.baeldung.com/jpa-composite-primary-keys */
/* Ref: https://attacomsian.com/blog/spring-data-jpa-composite-primary-key */
/*
* The composite primary key class must be public
* It must have a no-arg constructor
* It must define equals() and hashCode() methods
* It must be Serializable
*/
class FollowerId : Serializable {
        lateinit var email: String
        lateinit var follower: String

        override fun equals(other: Any?) =
                when {
                        this === other -> true
                        other !is FollowerId -> false
                        else -> other.email == this.email && other.follower == this.follower
                }

        override fun hashCode(): Int = Objects.hash(this.email + this.follower)
}
@Entity
@IdClass(FollowerId::class)
data class Follower(
        @Id
        var email: String,
        @Id
        var follower: String,
        @ManyToOne
        @JoinColumn(name = "poster", foreignKey = ForeignKey(name = "poster_fkey"))
        var user: User
)

/*
@Embeddable
class FollowerId(
        var follower: String,
        val email: String
) : Serializable
@Entity
data class Follower(
        @EmbeddedId
        var followerId: FollowerId,
        @ManyToOne
        @JoinColumn(name = "poster", foreignKey = ForeignKey(name = "poster_fkey"))
        var user: User
)
*/

@JsonRootName("profile")
data class Profile(
        val username: String,
        val bio: String = "",
        val image: String = "",
        val following: Boolean = false)

@JsonRootName("user")
sealed class Request {
        /* Properties that can be declared with val when using Reactive Route might because of using Reflection to map Request Body */
        @NotBlank(message = "email should not be blank")
        @Email(message = "email format is not valid", regexp = EMAIL_REGEX)
        var email = ""
        @NotBlank(message = "password should not be blank")
        @Size(message = "password should not less than 6 character", min = 6)
        var password = ""
}

class LoginReq : Request()

class RegisterReq : Request() {
        @NotBlank(message = "username should not be blank")
        @Size(message = "username should not less than 3 character", min = 3)
        @Pattern(message = "must be alphanumeric", regexp="^\\w+$")
        var username = ""
}

class UpdateReq {
        var email: String = ""
        var password: String = ""
        var username: String = ""
        var image: String = ""
        var bio: String = ""
}
